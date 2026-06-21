package com.cinemaweb.API.Cinema.Web.controller;


import com.cinemaweb.API.Cinema.Web.dto.request.SeatHoldRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatScheduleResponse;
import com.cinemaweb.API.Cinema.Web.service.SeatScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{scheduleId}")
    public ApiResponse<List<SeatScheduleResponse>> getListSeatScheduleBySchedule(
            @PathVariable int scheduleId) {
        return ApiResponse.<List<SeatScheduleResponse>>builder()
                .body(seatScheduleService.getListSeatScheduleBySchedule(scheduleId))
                .build();
    }

    // Giữ ghế tạm khi user bắt đầu thanh toán.
    @PostMapping("/hold")
    public ApiResponse<List<SeatScheduleResponse>> holdSeats(@RequestBody SeatHoldRequest request) {
        return ApiResponse.<List<SeatScheduleResponse>>builder()
                .body(seatScheduleService.holdSeats(request.getSeatScheduleIds()))
                .build();
    }

    // Nhả ghế đang giữ (ví dụ user huỷ thanh toán).
    @PostMapping("/release")
    public ApiResponse<Void> releaseSeats(@RequestBody SeatHoldRequest request) {
        seatScheduleService.releaseSeats(request.getSeatScheduleIds());
        return ApiResponse.<Void>builder().build();
    }
}
