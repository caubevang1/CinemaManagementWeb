package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.response.SeatScheduleResponse;
import com.cinemaweb.API.Cinema.Web.mapper.SeatScheduleMapper;
import com.cinemaweb.API.Cinema.Web.repository.SeatScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatScheduleService {
    @Autowired
    SeatScheduleRepository seatScheduleRepository;

    @Autowired
    SeatScheduleMapper seatScheduleMapper;

    public List<SeatScheduleResponse> getListSeatSchedule() {
        return seatScheduleMapper.toListSeatSchedule(seatScheduleRepository.findAll());
    }
}
