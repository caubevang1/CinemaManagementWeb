package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.MovieRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.enums.MovieStatus;
import com.cinemaweb.API.Cinema.Web.search.MovieSearchService;
import com.cinemaweb.API.Cinema.Web.service.MovieService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieController {
    @Autowired
    MovieService movieService;

    @Autowired
    MovieSearchService movieSearchService;

    // Tìm kiếm full-text qua RediSearch theo tên/thể loại/mô tả, lọc tùy chọn theo trạng thái.
    @GetMapping("/search")
    public ApiResponse<List<MovieResponse>> searchMovies(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) MovieStatus status) {
        return ApiResponse.<List<MovieResponse>>builder()
                .body(movieSearchService.search(q, status))
                .build();
    }

    // Gợi ý autocomplete tên phim theo prefix.
    @GetMapping("/suggest")
    public ApiResponse<List<String>> suggestMovies(
            @RequestParam(name = "q", required = false) String q) {
        return ApiResponse.<List<String>>builder()
                .body(movieSearchService.autocomplete(q))
                .build();
    }

    @GetMapping
    public ApiResponse<List<MovieResponse>> getAllMovies() {
        return ApiResponse.<List<MovieResponse>>builder()
                .body(movieService.getAllMovies())
                .build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<MovieResponse>> getAllMoviesForAdmin() {
        return ApiResponse.<List<MovieResponse>>builder()
                .body(movieService.getAllMoviesForAdmin())
                .build();
    }

    @GetMapping("/{movieid}")
    public ApiResponse<MovieResponse> getMovie(@PathVariable String movieid) {
        return ApiResponse.<MovieResponse>builder()
                .body(movieService.getMovie(movieid))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createMovie(@RequestBody @Valid MovieRequest movieCreateRequest) {
        movieService.createMovie(movieCreateRequest);
        return ApiResponse.<Void>builder().message("Film has been created").build();
    }

    @PutMapping("{movieid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MovieResponse> updateMovie(@PathVariable String movieid, @RequestBody MovieRequest movieUpdateRequest) {
        return ApiResponse.<MovieResponse>builder()
                .body(movieService.updateMovie(movieid, movieUpdateRequest))
                .build();
    }

    @DeleteMapping("{movieid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteMovie(@PathVariable String movieid) {
        movieService.deleteMovie(movieid);
        return ApiResponse.<Void>builder().message("Film with id " + movieid + " has been deleted").build();
    }
}
