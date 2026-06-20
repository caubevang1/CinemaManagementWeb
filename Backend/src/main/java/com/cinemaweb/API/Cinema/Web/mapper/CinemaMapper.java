package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.CinemaRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.CinemaResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CinemaMapper {
    CinemaResponse toCinemaResponse(Cinema cinema);

    Cinema toCinema(CinemaRequest cinemaCreateRequest);

    void toUpdateCinema(@MappingTarget Cinema cinema, CinemaRequest cinemaUpdateRequest);

    List<CinemaResponse> toCinemaResponseList(List<Cinema> cinema);

}
