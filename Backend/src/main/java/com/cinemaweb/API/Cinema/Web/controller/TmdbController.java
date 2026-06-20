package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.TmdbMovieResult;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.MovieMapper;
import com.cinemaweb.API.Cinema.Web.repository.MovieRepository;
import com.cinemaweb.API.Cinema.Web.service.TmdbService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tmdb")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TmdbController {

    TmdbService tmdbService;
    MovieRepository movieRepository;
    MovieMapper movieMapper;

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<TmdbMovieResult>> searchMovies(@RequestParam String query) {
        return ApiResponse.<List<TmdbMovieResult>>builder()
                .body(tmdbService.searchMovies(query))
                .build();
    }

    @GetMapping("/detail/{tmdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TmdbMovieResult> getDetail(@PathVariable int tmdbId) {
        return ApiResponse.<TmdbMovieResult>builder()
                .body(tmdbService.getMovieDetail(tmdbId))
                .build();
    }

    @PostMapping("/import/{tmdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MovieResponse> importMovie(@PathVariable int tmdbId) {
        TmdbMovieResult detail = tmdbService.getMovieDetail(tmdbId);

        if (movieRepository.existsByTmdbId(detail.getTmdbId())) {
            throw new AppException(ErrorCode.TMDB_MOVIE_EXISTED);
        }

        Movie movie = new Movie();
        movie.setTmdbId(detail.getTmdbId());
        movie.setMovieName(detail.getTitle());
        movie.setMovieDescription(detail.getOverview());
        movie.setMoviePoster(detail.getPosterUrl());
        movie.setMovieGenre(detail.getGenreNames());
        movie.setMovieLength(detail.getRuntime());
        movie.setMovieReview(detail.getVoteAverage() / 2);
        movie.setTrailerUrl(detail.getTrailerUrl());
        movie.setReleaseDate(detail.getReleaseDate());

        return ApiResponse.<MovieResponse>builder()
                .body(movieMapper.toMovieResponse(movieRepository.save(movie)))
                .message("Import phim thành công")
                .build();
    }
}
