package com.cinemaweb.API.Cinema.Web.search;

import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import com.cinemaweb.API.Cinema.Web.enums.MovieStatus;
import com.cinemaweb.API.Cinema.Web.repository.MovieRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Đồng bộ + truy vấn chỉ mục tìm kiếm phim bằng RediSearch qua Redisson ({@link RSearch}).
 *
 * <p>MySQL là nguồn dữ liệu chính; mỗi phim được phản chiếu sang một hash {@code movie:idx:{id}}
 * và index {@code movieIdx} (ON HASH) để full-text. Lỗi đồng bộ chỉ log, không làm hỏng luồng CRUD chính.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieSearchService {

    static final String INDEX = "movieIdx";
    static final String PREFIX = "movie:idx:";

    RedissonClient redisson;
    MovieRepository movieRepository;

    private RSearch search() {
        return redisson.getSearch(StringCodec.INSTANCE);
    }

    /** Tạo index nếu chưa có (ON HASH, prefix movie:idx:). */
    private void ensureIndex() {
        search().createIndex(
                INDEX,
                IndexOptions.defaults().on(IndexType.HASH).prefix(PREFIX),
                FieldIndex.text("movieName"),
                FieldIndex.text("movieGenre"),
                FieldIndex.text("movieDescription"),
                FieldIndex.tag("status"));
    }

    /** Nạp lại toàn bộ index từ MySQL (gọi lúc khởi động). */
    public void reindexAll() {
        // Xóa index cũ + docs rồi tạo lại để tránh schema/doc cũ lệch.
        try {
            search().dropIndex(INDEX);
        } catch (Exception ignored) {
            // index chưa tồn tại — bỏ qua
        }
        redisson.getKeys().deleteByPattern(PREFIX + "*");
        ensureIndex();

        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            writeDoc(movie);
        }
        log.info("RediSearch (Redisson): reindexed {} movies", movies.size());
    }

    /** Thêm/cập nhật một phim trong index. */
    public void upsert(Movie movie) {
        try {
            writeDoc(movie);
        } catch (Exception e) {
            log.error("RediSearch upsert failed for movieId={}", movie.getMovieId(), e);
        }
    }

    /** Gỡ một phim khỏi index. */
    public void remove(int movieId) {
        try {
            redisson.getKeys().delete(PREFIX + movieId);
        } catch (Exception e) {
            log.error("RediSearch remove failed for movieId={}", movieId, e);
        }
    }

    private void writeDoc(Movie movie) {
        RMap<String, String> doc = redisson.getMap(PREFIX + movie.getMovieId(), StringCodec.INSTANCE);
        Map<String, String> fields = new LinkedHashMap<>();
        put(fields, "movieId", String.valueOf(movie.getMovieId()));
        put(fields, "movieName", movie.getMovieName());
        put(fields, "movieGenre", movie.getMovieGenre());
        put(fields, "movieDescription", movie.getMovieDescription());
        put(fields, "status", movie.getStatus() != null ? movie.getStatus().name() : null);
        put(fields, "moviePoster", movie.getMoviePoster());
        put(fields, "movieLength", String.valueOf(movie.getMovieLength()));
        put(fields, "movieReview", movie.getMovieReview() != null ? movie.getMovieReview().toString() : null);
        put(fields, "tmdbId", movie.getTmdbId() != null ? movie.getTmdbId().toString() : null);
        put(fields, "trailerUrl", movie.getTrailerUrl());
        put(fields, "releaseDate", movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null);
        doc.delete(); // xóa field cũ trước khi ghi đè (tránh sót field khi update bỏ trống)
        doc.putAll(fields);
    }

    private static void put(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /** Tìm phim theo từ khóa (prefix full-text trên tên/thể loại/mô tả), lọc tùy chọn theo trạng thái. */
    public List<MovieResponse> search(String q, MovieStatus status) {
        String term = sanitize(q);
        if (term.isEmpty()) {
            return Collections.emptyList();
        }
        String query = "(@movieName:" + term + "*) | (@movieGenre:" + term + "*) | (@movieDescription:" + term + "*)";
        if (status != null) {
            query = "(" + query + ") @status:{" + status.name() + "}";
        }
        SearchResult result = search().search(INDEX, query, QueryOptions.defaults());
        return result.getDocuments().stream()
                .map(MovieSearchService::toResponse)
                .collect(Collectors.toList());
    }

    /** Gợi ý tên phim cho autocomplete (prefix theo tên). */
    public List<String> autocomplete(String prefix) {
        String term = sanitize(prefix);
        if (term.isEmpty()) {
            return Collections.emptyList();
        }
        SearchResult result = search().search(INDEX, "@movieName:" + term + "*", QueryOptions.defaults());
        return result.getDocuments().stream()
                .map(doc -> str(doc, "movieName"))
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
    }

    // Bỏ ký tự đặc biệt của cú pháp RediSearch để tránh lỗi query / injection; giữ chữ-số-khoảng trắng.
    private static String sanitize(String q) {
        if (q == null) {
            return "";
        }
        return q.replaceAll("[^\\p{L}\\p{N} ]", " ").trim();
    }

    private static MovieResponse toResponse(Document doc) {
        return MovieResponse.builder()
                .movieId(parseInt(str(doc, "movieId")))
                .movieName(str(doc, "movieName"))
                .moviePoster(str(doc, "moviePoster"))
                .movieGenre(str(doc, "movieGenre"))
                .movieLength(parseInt(str(doc, "movieLength")))
                .movieDescription(str(doc, "movieDescription"))
                .movieReview(parseDouble(str(doc, "movieReview")))
                .tmdbId(parseInteger(str(doc, "tmdbId")))
                .trailerUrl(str(doc, "trailerUrl"))
                .releaseDate(parseDate(str(doc, "releaseDate")))
                .status(parseStatus(str(doc, "status")))
                .build();
    }

    private static String str(Document doc, String key) {
        Object v = doc.getAttributes().get(key);
        return v != null ? v.toString() : null;
    }

    private static int parseInt(String s) {
        return s != null ? Integer.parseInt(s) : 0;
    }

    private static Integer parseInteger(String s) {
        return s != null ? Integer.valueOf(s) : null;
    }

    private static Double parseDouble(String s) {
        return s != null ? Double.valueOf(s) : null;
    }

    private static LocalDate parseDate(String s) {
        return s != null ? LocalDate.parse(s) : null;
    }

    private static MovieStatus parseStatus(String s) {
        return s != null ? MovieStatus.valueOf(s) : null;
    }
}
