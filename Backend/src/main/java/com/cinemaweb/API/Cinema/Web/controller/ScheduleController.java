package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.ScheduleRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.ScheduleResponse;
import com.cinemaweb.API.Cinema.Web.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ApiResponse<List<ScheduleResponse>> getAllSchedule() {
        return ApiResponse.<List<ScheduleResponse>>builder()
                .body(scheduleService.getAllSchedule())
                .build();
    }

    @GetMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> getSchedule(@PathVariable String scheduleId) {
        return ApiResponse.<ScheduleResponse>builder()
                .body(scheduleService.getSchedule(scheduleId))
                .build();
    }

    @PostMapping
    public ApiResponse<Void> createSchedule(@RequestBody @Valid ScheduleRequest scheduleCreate) {
        scheduleService.createSchedule(scheduleCreate);
        return ApiResponse.<Void>builder().message("Schedule has been created!").build();
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateSchedule(@RequestBody @Valid ScheduleRequest scheduleUpdate, @PathVariable String scheduleId) {
        scheduleService.updateSchedule(scheduleId, scheduleUpdate);
        return ApiResponse.<Void>builder().message("Update schedule with id " + scheduleId + " finish!").build();
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSchedule(@PathVariable String scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ApiResponse.<Void>builder().message("Delete schedule finish!").build();
    }
}
