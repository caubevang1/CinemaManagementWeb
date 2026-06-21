package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.Movie;
import com.cinemaweb.API.Cinema.Web.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie,String> {
    boolean existsByMovieNameAndReleaseDate(String movieName, LocalDate releaseDate);

    boolean existsByTmdbId(Integer tmdbId);

    // Phim hiển thị public: loại trừ trạng thái đã cho (ENDED).
    List<Movie> findByStatusNot(MovieStatus status);
}
