package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.SeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatResponse;
import com.cinemaweb.API.Cinema.Web.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
public class SeatController {
    @Autowired
    private SeatService seatService;

    @GetMapping
    public ApiResponse<List<SeatResponse>> getAllSeats() {
        return ApiResponse.<List<SeatResponse>>builder()
                .body(seatService.getAllSeats())
                .build();
    }

    @GetMapping("/{seatId}")
    public ApiResponse<SeatResponse> getSeat(@PathVariable String seatId) {
        return ApiResponse.<SeatResponse>builder()
                .body(seatService.getSeat(seatId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createSeat(@RequestBody @Valid SeatRequest seatCreateRequest) {
        seatService.createSeat(seatCreateRequest);
        return ApiResponse.<Void>builder().message("Seat has been created!").build();
    }

    @PutMapping("/{seatId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateSeat(@RequestBody @Valid SeatRequest seatUpdateRequest, @PathVariable String seatId) {
        seatService.updateSeat(seatId, seatUpdateRequest);
        return ApiResponse.<Void>builder().message("Seat with id " + seatId + " has been updated!").build();
    }

    @DeleteMapping("/{seatId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSeat(@PathVariable String seatId) {
        seatService.deleteSeat(seatId);
        return ApiResponse.<Void>builder().message("Seat with id " + seatId + " has been deleted!").build();
    }
}
