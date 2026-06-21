package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.ScheduleRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ScheduleResponse;
import com.cinemaweb.API.Cinema.Web.entity.*;
import com.cinemaweb.API.Cinema.Web.enums.SeatState;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.ScheduleMapper;
import com.cinemaweb.API.Cinema.Web.mapper.SeatScheduleMapper;
import com.cinemaweb.API.Cinema.Web.repository.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    ScheduleMapper scheduleMapper;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    SeatScheduleRepository seatScheduleRepository;

    @Autowired
    SeatScheduleMapper seatScheduleMapper;

    @Autowired
    SeatRepository seatRepository;

    public List<ScheduleResponse> getAllSchedule() {
        return scheduleMapper.toScheduleResponseList(scheduleRepository.findAll());
    }

    public ScheduleResponse getSchedule(String scheduleId) {
        return scheduleMapper.toScheduleResponse(scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule id is not found!")));
    }

    public void createSchedule(ScheduleRequest scheduleCreateRequest) {
        Schedule schedule = scheduleMapper.toCreateSchedule(scheduleCreateRequest);

        // Chặn 2 suất cùng phòng trùng khung giờ (DB không có exclusion constraint).
        if (scheduleRepository.existsOverlappingSchedule(
                schedule.getRoom().getRoomId(),
                schedule.getScheduleStart(),
                schedule.getScheduleEnd())) {
            throw new AppException(ErrorCode.SCHEDULE_TIME_OVERLAP);
        }

        scheduleRepository.save(schedule);

        int roomId = schedule.getRoom().getRoomId();
        //Create SeatSchedule: mỗi ghế trong phòng -> 1 dòng AVAILABLE, giá khởi tạo từ seat.
        List<Seat> seats = seatRepository.findByRoom_RoomId(roomId);
        List<SeatSchedule> seatSchedules = new ArrayList<>();
        for (Seat seat : seats) {
            SeatSchedule seatSchedule = SeatSchedule.builder()
                    .schedule(schedule)
                    .seat(seat)
                    .seatState(SeatState.AVAILABLE)
                    .price(seat.getSeatPrice())
                    .build();
            seatSchedules.add(seatSchedule);
        }
        seatScheduleRepository.saveAll(seatSchedules);
    }

    public void updateSchedule(String scheduleId, ScheduleRequest scheduleUpdateRequest) {
        Room room = roomRepository.findById(Integer.toString(scheduleUpdateRequest.getRoomId()))
                .orElseThrow(() -> new RuntimeException("Room id in updateSchedule is not found!"));

        Movie movie = movieRepository.findById(Integer.toString(scheduleUpdateRequest.getMovieId()))
                .orElseThrow(() -> new RuntimeException("Movie id in updateSchedule is not found!"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule id in updateSchedule is not found!"));

        // Cinema suy ra từ room — không set trực tiếp nữa.
        schedule.setMovie(movie);
        schedule.setRoom(room);
        scheduleMapper.toUpdateSchedule(schedule, scheduleUpdateRequest);

        // Chặn trùng khung giờ, loại trừ chính suất đang cập nhật.
        if (scheduleRepository.existsOverlappingScheduleExcluding(
                room.getRoomId(),
                schedule.getScheduleStart(),
                schedule.getScheduleEnd(),
                schedule.getScheduleId())) {
            throw new AppException(ErrorCode.SCHEDULE_TIME_OVERLAP);
        }

        scheduleRepository.save(schedule);
    }

    public void deleteSchedule(String scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}
