package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(@PathVariable String bookingId) {
        return ApiResponse.<BookingResponse>builder()
                .body(bookingService.getBooking(bookingId))
                .build();
    }

    @PostMapping
    public ApiResponse<Void> createBooking(@RequestBody @Valid BookingRequest bookingRequest) {
        bookingService.createBooking(bookingRequest);
        return ApiResponse.<Void>builder().message("Creation booking finished").build();
    }

    @GetMapping("/myBooking")
    public ApiResponse<List<BookingResponse>> getAllMyBooking() {
        return ApiResponse.<List<BookingResponse>>builder()
                .body(bookingService.getAllMyBooking())
                .build();
    }
}
