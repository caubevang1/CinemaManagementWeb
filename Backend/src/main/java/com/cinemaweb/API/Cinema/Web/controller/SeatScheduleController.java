package com.cinemaweb.API.Cinema.Web.controller;


import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatScheduleResponse;
import com.cinemaweb.API.Cinema.Web.service.SeatScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seatSchedule")
public class SeatScheduleController {
    @Autowired
    private SeatScheduleService seatScheduleService;

    @GetMapping
    public ApiResponse<List<SeatScheduleResponse>> getListSeatSchedule() {
        return ApiResponse.<List<SeatScheduleResponse>>builder()
                .body(seatScheduleService.getListSeatSchedule())
                .build();
    }
}
