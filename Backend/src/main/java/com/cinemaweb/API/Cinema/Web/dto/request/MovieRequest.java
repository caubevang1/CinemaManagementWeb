package com.cinemaweb.API.Cinema.Web.dto.request;

import com.cinemaweb.API.Cinema.Web.enums.MovieStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieRequest {
    @Size(max = 50, message = "Max length film name is  50 character")
    String movieName;

    String moviePoster;
    String movieGenre;
    int movieLength;
    String movieDescription;
    Double movieReview;

    Integer tmdbId;
    String trailerUrl;

    @NotNull(message = "RELEASE_DATE_NULL")
    LocalDate releaseDate;

    MovieStatus status;
}
