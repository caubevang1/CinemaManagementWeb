package com.cinemaweb.API.Cinema.Web.controller;


import com.cinemaweb.API.Cinema.Web.dto.request.BookingSeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingSeatResponse;
import com.cinemaweb.API.Cinema.Web.service.BookingSeatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookingSeat")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BookingSeatController {
    BookingSeatService bookingSeatService;

    @GetMapping("/{bookingSeatId}")
    public ApiResponse<BookingSeatResponse> getBookingSeat(@PathVariable("bookingSeatId") int id)  {
        return ApiResponse.<BookingSeatResponse>builder()
                .body(bookingSeatService.get(id))
                .build();
    }

    @PostMapping
    public ApiResponse<BookingSeatResponse> createBookingSeat(@RequestBody BookingSeatRequest request) {
        return ApiResponse.<BookingSeatResponse>builder()
                .body(bookingSeatService.create(request))
                .build();
    }
}
