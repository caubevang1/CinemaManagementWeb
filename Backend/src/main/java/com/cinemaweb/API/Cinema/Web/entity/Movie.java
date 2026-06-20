package com.cinemaweb.API.Cinema.Web.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
// Ràng buộc unique được thực thi ở DB (xem database/cinema.sql). Khai báo ở đây để tài liệu hóa;
// KHÔNG tự áp dụng vì ddl-auto = none.
@Table(name = "movie", uniqueConstraints = {
        @UniqueConstraint(name = "uq_movie_name_release_date", columnNames = {"movie_name", "release_date"}),
        @UniqueConstraint(name = "uq_movie_tmdb_id", columnNames = {"tmdb_id"})
})
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Movie {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int movieId;

    String movieName;
    String moviePoster;
    String movieGenre;
    int movieLength;
    String movieDescription;
    Double movieReview;

    Integer tmdbId;
    String trailerUrl;
    LocalDate releaseDate;
}
