package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.MovieRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
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

    @GetMapping
    public ApiResponse<List<MovieResponse>> getAllMovies() {
        return ApiResponse.<List<MovieResponse>>builder()
                .body(movieService.getAllMovies())
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
