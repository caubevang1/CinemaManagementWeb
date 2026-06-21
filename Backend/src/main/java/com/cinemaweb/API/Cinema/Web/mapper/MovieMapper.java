package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.MovieRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.MovieResponse;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    MovieResponse toMovieResponse(Movie movie);

    Movie toMovie(MovieRequest MovieCreateRequest);

    // Bỏ qua các field null trong request để cập nhật một phần không xóa dữ liệu cũ
    // (vd form Edit không gửi releaseDate/status thì giữ nguyên giá trị hiện tại).
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMovie(@MappingTarget Movie movie, MovieRequest movieUpdateRequest);

    List<MovieResponse> toMovieResponseList(List<Movie> movies);
}
