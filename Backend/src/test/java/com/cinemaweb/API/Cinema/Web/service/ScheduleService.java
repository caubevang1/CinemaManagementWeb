package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.ScheduleRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ScheduleResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import com.cinemaweb.API.Cinema.Web.entity.Room;
import com.cinemaweb.API.Cinema.Web.entity.Schedule;
import com.cinemaweb.API.Cinema.Web.mapper.ScheduleMapper;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import com.cinemaweb.API.Cinema.Web.repository.MovieRepository;
import com.cinemaweb.API.Cinema.Web.repository.RoomRepository;
import com.cinemaweb.API.Cinema.Web.repository.ScheduleRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    ScheduleMapper scheduleMapper;

    @Autowired
    CinemaRepository cinemaRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MovieRepository movieRepository;

    public List<ScheduleResponse> getAllSchedule() {
        return scheduleMapper.toScheduleResponseList(scheduleRepository.findAll());
    }

    public ScheduleResponse getSchedule(String scheduleId) {
        return scheduleMapper.toScheduleResponse(scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule id is not found!")));
    }

    public void createSchedule(ScheduleRequest scheduleCreateRequest) {
        Schedule schedule = scheduleMapper.toCreateSchedule(scheduleCreateRequest);
        scheduleRepository.save(schedule);
    }

    public void updateSchedule(String scheduleId, ScheduleRequest scheduleUpdateRequest) {
        Cinema cinema = cinemaRepository.findById(Integer.toString(scheduleUpdateRequest.getCinemaId()))
                .orElseThrow(() -> new RuntimeException("Cinema id in updateSchedule is not found!"));

        Room room = roomRepository.findById(Integer.toString(scheduleUpdateRequest.getRoomId()))
                .orElseThrow(() -> new RuntimeException("Room id in updateSchedule is not found!"));

        Movie movie = movieRepository.findById(Integer.toString(scheduleUpdateRequest.getMovieId()))
                .orElseThrow(() -> new RuntimeException("Movie id in updateSchedule is not found!"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule id in updateSchedule is not found!"));

        schedule.setCinema(cinema);
        schedule.setMovie(movie);
        schedule.setRoom(room);
        scheduleMapper.toUpdateSchedule(schedule,scheduleUpdateRequest);
        scheduleRepository.save(schedule);
    }

    public void deleteSchedule(String scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}
