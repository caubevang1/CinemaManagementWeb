package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.MovieRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import com.cinemaweb.API.Cinema.Web.enums.MovieStatus;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.configuration.CacheConfig;
import com.cinemaweb.API.Cinema.Web.mapper.MovieMapper;
import com.cinemaweb.API.Cinema.Web.repository.MovieRepository;
import com.cinemaweb.API.Cinema.Web.search.MovieSearchService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieService {
    @Autowired
    MovieRepository movieRepository;

    @Autowired
    MovieMapper movieMapper;

    @Autowired
    MovieSearchService movieSearchService;

    // Public: chỉ trả phim không bị ENDED (đang chiếu + sắp chiếu).
    @Cacheable(value = CacheConfig.MOVIES, key = "'public'")
    public List<MovieResponse> getAllMovies() {
        return movieMapper.toMovieResponseList(movieRepository.findByStatusNot(MovieStatus.ENDED));
    }

    // Admin: trả tất cả phim kể cả ENDED để quản lý.
    @Cacheable(value = CacheConfig.MOVIES, key = "'admin'")
    public List<MovieResponse> getAllMoviesForAdmin() {
        return movieMapper.toMovieResponseList(movieRepository.findAll());
    }

    @Cacheable(value = CacheConfig.MOVIE, key = "#movieId")
    public MovieResponse getMovie(String movieId) {
        return movieMapper.toMovieResponse(movieRepository.findById(movieId).orElseThrow(()
                -> new RuntimeException("Movie id not found")));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.MOVIES, allEntries = true),
            @CacheEvict(value = CacheConfig.MOVIE, allEntries = true)
    })
    public void createMovie(MovieRequest movieCreateRequest) {
        if (movieRepository.existsByMovieNameAndReleaseDate(
                movieCreateRequest.getMovieName(), movieCreateRequest.getReleaseDate())) {
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }
        Movie movie = movieMapper.toMovie(movieCreateRequest);
        if (movie.getStatus() == null) {
            movie.setStatus(MovieStatus.NOW_SHOWING);
        }
        Movie saved = movieRepository.save(movie);
        movieSearchService.upsert(saved); // đồng bộ chỉ mục tìm kiếm
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.MOVIES, allEntries = true),
            @CacheEvict(value = CacheConfig.MOVIE, key = "#movieId")
    })
    public MovieResponse updateMovie(String movieId, MovieRequest movieUpdateRequest) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(()
                -> new RuntimeException("Movie id not found"));

        movieMapper.updateMovie(movie, movieUpdateRequest);
        Movie saved = movieRepository.save(movie);
        movieSearchService.upsert(saved); // đồng bộ chỉ mục tìm kiếm
        return movieMapper.toMovieResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.MOVIES, allEntries = true),
            @CacheEvict(value = CacheConfig.MOVIE, key = "#movieId")
    })
    public void deleteMovie(String movieId) {
        movieRepository.deleteById(movieId);
        movieSearchService.remove(Integer.parseInt(movieId)); // gỡ khỏi chỉ mục tìm kiếm
    }
}
