package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.MovieRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
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

    public List<MovieResponse> getAllMovies() {
        return movieMapper.toMovieResponseList(movieRepository.findAll());
    }

    public MovieResponse getMovie(String movieId) {
        return movieMapper.toMovieResponse(movieRepository.findById(movieId).orElseThrow(()
                -> new RuntimeException("Movie id not found")));
    }

    public void createMovie(MovieRequest movieCreateRequest) {
        movieRepository.save(movieMapper.toMovie(movieCreateRequest));
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
