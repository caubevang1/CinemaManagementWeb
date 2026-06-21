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
    // Cinema suy ra qua room.cinema (schedule không còn cột cinema_id).
    @Mapping(source = "movie.movieName", target = "movieName")
    @Mapping(source = "room.roomName", target = "roomName")
    @Mapping(source = "room.cinema.cinemaName", target = "cinemaName")
    @Mapping(source = "room.roomId", target = "roomId")
    @Mapping(source = "movie.movieId", target = "movieId")
    @Mapping(source = "room.cinema.cinemaId", target = "cinemaId")
    ScheduleResponse toScheduleResponse(Schedule schedule);

    List<ScheduleResponse> toScheduleResponseList(List<Schedule> scheduleList);

    @Mapping(source = "movieId", target = "movie.movieId")
    @Mapping(source = "roomId", target = "room.roomId")
    Schedule toCreateSchedule(ScheduleRequest scheduleRequest);

    @Mapping(target = "scheduleId", ignore=true)
    @Mapping(target = "movie.movieId", ignore=true)
    @Mapping(target = "room.roomId", ignore=true)
    void toUpdateSchedule(@MappingTarget Schedule schedule, ScheduleRequest scheduleUpdateRequest);
}
