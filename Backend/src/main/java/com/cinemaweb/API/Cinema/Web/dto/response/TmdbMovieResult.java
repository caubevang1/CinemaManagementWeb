package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TmdbMovieResult {
    int tmdbId;
    String title;
    String overview;
    String posterUrl;
    String backdropUrl;
    String originalLanguage;
    Double voteAverage;
    LocalDate releaseDate;
    String genreNames;
    int runtime;
    String trailerUrl;
}
