package com.cinemaweb.API.Cinema.Web.search;

import com.cinemaweb.API.Cinema.Web.dto.response.FriendSearchResponse;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
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
 * Đồng bộ + truy vấn chỉ mục tìm kiếm user bằng RediSearch qua Redisson ({@link RSearch}).
 *
 * <p>MySQL là nguồn dữ liệu chính; mỗi user được phản chiếu sang một hash {@code user:idx:{id}}
 * và index {@code userIdx} (ON HASH) để full-text theo username/email/tên. Thay cho truy vấn
 * SQL {@code LIKE '%q%'} (leading wildcard, không dùng được index). Lỗi đồng bộ chỉ log,
 * không làm hỏng luồng CRUD chính.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSearchService {

    static final String INDEX = "userIdx";
    static final String PREFIX = "user:idx:";

    RedissonClient redisson;
    UserRepository userRepository;

    private RSearch search() {
        return redisson.getSearch(StringCodec.INSTANCE);
    }

    /** Tạo index nếu chưa có (ON HASH, prefix user:idx:). */
    private void ensureIndex() {
        search().createIndex(
                INDEX,
                IndexOptions.defaults().on(IndexType.HASH).prefix(PREFIX),
                FieldIndex.text("username"),
                FieldIndex.text("email"),
                FieldIndex.text("firstName"),
                FieldIndex.text("lastName"));
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

        List<User> users = userRepository.findAll();
        for (User user : users) {
            writeDoc(user);
        }
        log.info("RediSearch (Redisson): reindexed {} users", users.size());
    }

    /** Thêm/cập nhật một user trong index. */
    public void upsert(User user) {
        try {
            writeDoc(user);
        } catch (Exception e) {
            log.error("RediSearch upsert failed for userId={}", user.getID(), e);
        }
    }

    /** Gỡ một user khỏi index. */
    public void remove(String userId) {
        try {
            redisson.getKeys().delete(PREFIX + userId);
        } catch (Exception e) {
            log.error("RediSearch remove failed for userId={}", userId, e);
        }
    }

    private void writeDoc(User user) {
        RMap<String, String> doc = redisson.getMap(PREFIX + user.getID(), StringCodec.INSTANCE);
        Map<String, String> fields = new LinkedHashMap<>();
        put(fields, "id", user.getID());
        put(fields, "username", user.getUsername());
        put(fields, "email", user.getEmail());
        put(fields, "firstName", user.getFirstName());
        put(fields, "lastName", user.getLastName());
        put(fields, "avatar", user.getAvatar());
        doc.delete(); // xóa field cũ trước khi ghi đè (tránh sót field khi update bỏ trống)
        doc.putAll(fields);
    }

    private static void put(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Tìm user theo từ khóa (prefix full-text trên username/email/tên).
     * Trả về {@link FriendSearchResponse} với {@code friendshipStatus = "NONE"};
     * việc enrich trạng thái quan hệ + sắp xếp do {@code UserService.searchUsers} đảm nhận.
     */
    public List<FriendSearchResponse> search(String q) {
        String term = sanitize(q);
        if (term.isEmpty()) {
            return Collections.emptyList();
        }
        String query = "(@username:" + term + "*) | (@email:" + term + "*)"
                + " | (@firstName:" + term + "*) | (@lastName:" + term + "*)";
        SearchResult result = search().search(INDEX, query, QueryOptions.defaults());
        return result.getDocuments().stream()
                .map(UserSearchService::toResponse)
                .collect(Collectors.toList());
    }

    // Bỏ ký tự đặc biệt của cú pháp RediSearch để tránh lỗi query / injection; giữ chữ-số-khoảng trắng.
    private static String sanitize(String q) {
        if (q == null) {
            return "";
        }
        return q.replaceAll("[^\\p{L}\\p{N} ]", " ").trim();
    }

    private static FriendSearchResponse toResponse(Document doc) {
        return FriendSearchResponse.builder()
                .id(str(doc, "id"))
                .username(str(doc, "username"))
                .firstName(str(doc, "firstName"))
                .lastName(str(doc, "lastName"))
                .avatar(str(doc, "avatar"))
                .email(str(doc, "email"))
                .friendshipStatus("NONE")
                .build();
    }

    private static String str(Document doc, String key) {
        Object v = doc.getAttributes().get(key);
        return v != null ? v.toString() : null;
    }
}
