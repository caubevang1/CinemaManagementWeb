package com.cinemaweb.API.Cinema.Web.search;

import com.cinemaweb.API.Cinema.Web.dto.response.CinemaResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.redisson.api.search.query.Document;
import org.redisson.api.search.query.QueryOptions;
import org.redisson.api.search.query.SearchResult;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Đồng bộ + truy vấn chỉ mục tìm kiếm rạp bằng RediSearch qua Redisson ({@link RSearch}).
 *
 * <p>MySQL là nguồn dữ liệu chính; mỗi rạp được phản chiếu sang một hash {@code cinema:idx:{id}}
 * và index {@code cinemaIdx} (ON HASH) để full-text theo tên/địa chỉ. Lỗi đồng bộ chỉ log,
 * không làm hỏng luồng CRUD chính.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CinemaSearchService {

    static final String INDEX = "cinemaIdx";
    static final String PREFIX = "cinema:idx:";

    RedissonClient redisson;
    CinemaRepository cinemaRepository;

    private RSearch search() {
        return redisson.getSearch(StringCodec.INSTANCE);
    }

    /** Tạo index nếu chưa có (ON HASH, prefix cinema:idx:). */
    private void ensureIndex() {
        search().createIndex(
                INDEX,
                IndexOptions.defaults().on(IndexType.HASH).prefix(PREFIX),
                FieldIndex.text("cinemaName"),
                FieldIndex.text("cinemaAddress"));
    }

    /** Nạp lại toàn bộ index từ MySQL (gọi lúc khởi động). */
    public void reindexAll() {
        try {
            search().dropIndex(INDEX);
        } catch (Exception ignored) {
            // index chưa tồn tại — bỏ qua
        }
        redisson.getKeys().deleteByPattern(PREFIX + "*");
        ensureIndex();

        List<Cinema> cinemas = cinemaRepository.findAll();
        for (Cinema cinema : cinemas) {
            writeDoc(cinema);
        }
        log.info("RediSearch (Redisson): reindexed {} cinemas", cinemas.size());
    }

    /** Thêm/cập nhật một rạp trong index. */
    public void upsert(Cinema cinema) {
        try {
            writeDoc(cinema);
        } catch (Exception e) {
            log.error("RediSearch upsert failed for cinemaId={}", cinema.getCinemaId(), e);
        }
    }

    /** Gỡ một rạp khỏi index. */
    public void remove(int cinemaId) {
        try {
            redisson.getKeys().delete(PREFIX + cinemaId);
        } catch (Exception e) {
            log.error("RediSearch remove failed for cinemaId={}", cinemaId, e);
        }
    }

    private void writeDoc(Cinema cinema) {
        RMap<String, String> doc = redisson.getMap(PREFIX + cinema.getCinemaId(), StringCodec.INSTANCE);
        Map<String, String> fields = new LinkedHashMap<>();
        put(fields, "cinemaId", String.valueOf(cinema.getCinemaId()));
        put(fields, "cinemaName", cinema.getCinemaName());
        put(fields, "cinemaAddress", cinema.getCinemaAddress());
        doc.delete(); // xóa field cũ trước khi ghi đè (tránh sót field khi update bỏ trống)
        doc.putAll(fields);
    }

    private static void put(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /** Tìm rạp theo từ khóa (prefix full-text trên tên/địa chỉ). */
    public List<CinemaResponse> search(String q) {
        String term = sanitize(q);
        if (term.isEmpty()) {
            return Collections.emptyList();
        }
        String query = "(@cinemaName:" + term + "*) | (@cinemaAddress:" + term + "*)";
        SearchResult result = search().search(INDEX, query, QueryOptions.defaults());
        return result.getDocuments().stream()
                .map(CinemaSearchService::toResponse)
                .collect(Collectors.toList());
    }

    // Bỏ ký tự đặc biệt của cú pháp RediSearch để tránh lỗi query / injection; giữ chữ-số-khoảng trắng.
    private static String sanitize(String q) {
        if (q == null) {
            return "";
        }
        return q.replaceAll("[^\\p{L}\\p{N} ]", " ").trim();
    }

    private static CinemaResponse toResponse(Document doc) {
        return CinemaResponse.builder()
                .cinemaId(parseInt(str(doc, "cinemaId")))
                .cinemaName(str(doc, "cinemaName"))
                .cinemaAddress(str(doc, "cinemaAddress"))
                .build();
    }

    private static String str(Document doc, String key) {
        Object v = doc.getAttributes().get(key);
        return v != null ? v.toString() : null;
    }

    private static int parseInt(String s) {
        return s != null ? Integer.parseInt(s) : 0;
    }
}
