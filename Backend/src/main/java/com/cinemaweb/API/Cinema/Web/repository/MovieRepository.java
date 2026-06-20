package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MovieRepository extends JpaRepository<Movie,String> {
    boolean existsByMovieNameAndReleaseDate(String movieName, LocalDate releaseDate);

    boolean existsByTmdbId(Integer tmdbId);
}
