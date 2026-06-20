package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.MovieRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    MovieResponse toMovieResponse(Movie movie);

    Movie toMovie(MovieRequest MovieCreateRequest);

    void updateMovie(@MappingTarget Movie movie, MovieRequest movieUpdateRequest);

    List<MovieResponse> toMovieResponseList(List<Movie> movies);
}
