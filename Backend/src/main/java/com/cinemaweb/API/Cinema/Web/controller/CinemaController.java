package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.CinemaRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.CinemaResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.service.CinemaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cinemas")
public class CinemaController {
    @Autowired
    private CinemaService cinemaService;

    @GetMapping
    public ApiResponse<List<CinemaResponse>> getAllCinemas() {
        return ApiResponse.<List<CinemaResponse>>builder()
                .body(cinemaService.getAllCinemas())
                .build();
    }

    @GetMapping("/{cinemaId}")
    public ApiResponse<CinemaResponse> getCinema(@PathVariable String cinemaId) {
        return ApiResponse.<CinemaResponse>builder()
                .body(cinemaService.getCinema(cinemaId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createCinema(@RequestBody @Valid CinemaRequest cinemaCreateRequest) {
        cinemaService.createCinema(cinemaCreateRequest);
        return ApiResponse.<Void>builder().message("Cinema has created").build();
    }

    @PutMapping("/{cinemaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateCinema(@RequestBody @Valid CinemaRequest cinemaUpdateRequest, @PathVariable String cinemaId) {
        cinemaService.updateCinema(cinemaId, cinemaUpdateRequest);
        return ApiResponse.<Void>builder().message("Update finish").build();
    }

    @DeleteMapping("/{cinemaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCinema(@PathVariable String cinemaId) {
        cinemaService.deleteCinema(cinemaId);
        return ApiResponse.<Void>builder().message("Cinema with id " + cinemaId + " has been deleted!").build();
    }
}
