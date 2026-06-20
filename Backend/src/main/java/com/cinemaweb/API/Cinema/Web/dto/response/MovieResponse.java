package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieResponse {
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
