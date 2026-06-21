package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.MovieRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import com.cinemaweb.API.Cinema.Web.enums.MovieStatus;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.MovieMapper;
import com.cinemaweb.API.Cinema.Web.repository.MovieRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieService {
    @Autowired
    MovieRepository movieRepository;

    @Autowired
    MovieMapper movieMapper;

    // Public: chỉ trả phim không bị ENDED (đang chiếu + sắp chiếu).
    public List<MovieResponse> getAllMovies() {
        return movieMapper.toMovieResponseList(movieRepository.findByStatusNot(MovieStatus.ENDED));
    }

    // Admin: trả tất cả phim kể cả ENDED để quản lý.
    public List<MovieResponse> getAllMoviesForAdmin() {
        return movieMapper.toMovieResponseList(movieRepository.findAll());
    }

    public MovieResponse getMovie(String movieId) {
        return movieMapper.toMovieResponse(movieRepository.findById(movieId).orElseThrow(()
                -> new RuntimeException("Movie id not found")));
    }

    public void createMovie(MovieRequest movieCreateRequest) {
        if (movieRepository.existsByMovieNameAndReleaseDate(
                movieCreateRequest.getMovieName(), movieCreateRequest.getReleaseDate())) {
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }
        Movie movie = movieMapper.toMovie(movieCreateRequest);
        if (movie.getStatus() == null) {
            movie.setStatus(MovieStatus.NOW_SHOWING);
        }
        movieRepository.save(movie);
    }

    public MovieResponse updateMovie(String movieId, MovieRequest movieUpdateRequest) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(()
                -> new RuntimeException("Movie id not found"));

        movieMapper.updateMovie(movie, movieUpdateRequest);
        return movieMapper.toMovieResponse(movieRepository.save(movie));
    }

    public void deleteMovie(String movieId) {
        movieRepository.deleteById(movieId);
    }
}
