package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.ScheduleRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ScheduleResponse;
import com.cinemaweb.API.Cinema.Web.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    @Mapping(source = "movie.movieName", target = "movieName")
    @Mapping(source = "room.roomName", target = "roomName")
    @Mapping(source = "cinema.cinemaName", target = "cinemaName")
    @Mapping(source = "room.roomId", target = "roomId")
    @Mapping(source = "movie.movieId", target = "movieId")
    @Mapping(source = "cinema.cinemaId", target = "cinemaId")
    ScheduleResponse toScheduleResponse(Schedule schedule);

    @Mapping(source = "movie.movieName", target = "movieName")
    @Mapping(source = "room.roomName", target = "roomName")
    @Mapping(source = "cinema.cinemaName", target = "cinemaName")
    @Mapping(source = "room.roomId", target = "roomId")
    @Mapping(source = "movie.movieId", target = "movieId")
    @Mapping(source = "cinema.cinemaId", target = "cinemaId")
    List<ScheduleResponse> toScheduleResponseList(List<Schedule> scheduleList);

    @Mapping(source = "movieId", target = "movie.movieId")
    @Mapping(source = "roomId", target = "room.roomId")
    @Mapping(source = "cinemaId", target = "cinema.cinemaId")
    Schedule toCreateSchedule(ScheduleRequest scheduleRequest);

    @Mapping(target = "scheduleId", ignore=true)
    @Mapping(target = "movie.movieId", ignore=true)
    @Mapping(target = "room.roomId", ignore=true)
    @Mapping(target = "cinema.cinemaId", ignore=true)
    void toUpdateSchedule(@MappingTarget Schedule schedule, ScheduleRequest scheduleUpdateRequest);
}
