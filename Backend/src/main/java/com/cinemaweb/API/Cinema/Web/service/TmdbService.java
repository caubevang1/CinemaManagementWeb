package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.response.TmdbMovieResult;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class TmdbService {

    @Value("${tmdb.api-key}")
    String apiKey;

    @Value("${tmdb.base-url}")
    String baseUrl;

    @Value("${tmdb.image-base-url}")
    String imageBaseUrl;

    final RestTemplate restTemplate = new RestTemplate();
    final ObjectMapper objectMapper = new ObjectMapper();

    @CircuitBreaker(name = "tmdb", fallbackMethod = "searchMoviesFallback")
    public List<TmdbMovieResult> searchMovies(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "vi-VN")
                .queryParam("include_adult", false)
                .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");

            List<TmdbMovieResult> movies = new ArrayList<>();
            if (results != null && results.isArray()) {
                for (JsonNode node : results) {
                    movies.add(buildBasicResult(node));
                }
            }
            return movies;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search TMDB: " + e.getMessage());
        }
    }

    @CircuitBreaker(name = "tmdb", fallbackMethod = "getMovieDetailFallback")
    public TmdbMovieResult getMovieDetail(int tmdbId) {
        String detailUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId)
                .queryParam("api_key", apiKey)
                .queryParam("language", "vi-VN")
                .toUriString();

        String videoUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId + "/videos")
                .queryParam("api_key", apiKey)
                .queryParam("language", "en-US")
                .toUriString();

        try {
            String detailResponse = restTemplate.getForObject(detailUrl, String.class);
            JsonNode detail = objectMapper.readTree(detailResponse);

            String trailerUrl = fetchTrailerUrl(videoUrl);

            String genres = "";
            JsonNode genreArray = detail.get("genres");
            if (genreArray != null && genreArray.isArray()) {
                genres = StreamSupport.stream(genreArray.spliterator(), false)
                        .map(g -> g.path("name").asText(""))
                        .filter(name -> !name.isBlank())
                        .collect(Collectors.joining(", "));
            }

            int runtime = detail.has("runtime") ? detail.get("runtime").asInt(0) : 0;

            String posterPath = detail.has("poster_path") && !detail.get("poster_path").isNull()
                    ? imageBaseUrl + detail.get("poster_path").asText() : "";

            String backdropPath = detail.has("backdrop_path") && !detail.get("backdrop_path").isNull()
                    ? imageBaseUrl + detail.get("backdrop_path").asText() : "";

            LocalDate releaseDate = parseDate(detail.has("release_date") ? detail.get("release_date").asText("") : "");

            return TmdbMovieResult.builder()
                    .tmdbId(tmdbId)
                    .title(detail.has("title") ? detail.get("title").asText("") : "")
                    .overview(detail.has("overview") ? detail.get("overview").asText("") : "")
                    .posterUrl(posterPath)
                    .backdropUrl(backdropPath)
                    .originalLanguage(detail.has("original_language") ? detail.get("original_language").asText("") : "")
                    .voteAverage(detail.has("vote_average") ? detail.get("vote_average").asDouble(0) : 0)
                    .releaseDate(releaseDate)
                    .genreNames(genres)
                    .runtime(runtime)
                    .trailerUrl(trailerUrl)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get TMDB movie detail: " + e.getMessage());
        }
    }

    private String fetchTrailerUrl(String videoUrl) {
        try {
            String response = restTemplate.getForObject(videoUrl, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");
            if (results != null && results.isArray()) {
                for (JsonNode video : results) {
                    String type = video.has("type") ? video.get("type").asText("") : "";
                    String site = video.has("site") ? video.get("site").asText("") : "";
                    if ("Trailer".equalsIgnoreCase(type) && "YouTube".equalsIgnoreCase(site)) {
                        return "https://www.youtube.com/watch?v=" + video.get("key").asText();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private TmdbMovieResult buildBasicResult(JsonNode node) {
        String posterPath = node.has("poster_path") && !node.get("poster_path").isNull()
                ? imageBaseUrl + node.get("poster_path").asText() : "";

        LocalDate releaseDate = parseDate(node.has("release_date") ? node.get("release_date").asText("") : "");

        return TmdbMovieResult.builder()
                .tmdbId(node.has("id") ? node.get("id").asInt(0) : 0)
                .title(node.has("title") ? node.get("title").asText("") : "")
                .overview(node.has("overview") ? node.get("overview").asText("") : "")
                .posterUrl(posterPath)
                .originalLanguage(node.has("original_language") ? node.get("original_language").asText("") : "")
                .voteAverage(node.has("vote_average") ? node.get("vote_average").asDouble(0) : 0)
                .releaseDate(releaseDate)
                .build();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private List<TmdbMovieResult> searchMoviesFallback(String query, Throwable t) {
        if (t instanceof CallNotPermittedException) {          // circuit ĐANG MỞ
            log.warn("TMDB circuit OPEN, searchMovies query={}", query);
            throw new AppException(ErrorCode.TMDB_UNAVAILABLE);
        }
        log.error("TMDB search failed query={}: {}", query, t.getMessage());
        throw new AppException(ErrorCode.TMDB_FETCH_FAILED);
    }

    private TmdbMovieResult getMovieDetailFallback(int tmdbId, Throwable t) {
        if (t instanceof CallNotPermittedException) {          // circuit ĐANG MỞ
            log.warn("TMDB circuit OPEN, getMovieDetail tmdbId={}", tmdbId);
            throw new AppException(ErrorCode.TMDB_UNAVAILABLE);
        }
        log.error("TMDB detail failed tmdbId={}: {}", tmdbId, t.getMessage());
        throw new AppException(ErrorCode.TMDB_FETCH_FAILED);
    }
}
