package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingFoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingFoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.service.BookingFoodAndDrinkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookingFoodAndDrink")
public class BookingFoodAndDrinkController {
    @Autowired
    private BookingFoodAndDrinkService bookingFoodAndDrinkService;

    @GetMapping
    public ApiResponse<List<BookingFoodAndDrinkResponse>> getListBookingFoodAndDrinks() {
        return ApiResponse.<List<BookingFoodAndDrinkResponse>>builder()
                .body(bookingFoodAndDrinkService.getListBookingFoodAndDrink())
                .build();
    }

    @GetMapping("/{bookingFoodAndDrinkId}")
    public ApiResponse<BookingFoodAndDrinkResponse> getBookingFoodAndDrink(@PathVariable String bookingFoodAndDrinkId) {
        return ApiResponse.<BookingFoodAndDrinkResponse>builder()
                .body(bookingFoodAndDrinkService.getBookingFoodAndDrink(bookingFoodAndDrinkId))
                .build();
    }

    @PostMapping
    public ApiResponse<Void> createBookingFoodAndDrink(@RequestBody @Valid BookingFoodAndDrinkRequest bookingFoodAndDrinkCreateRequest) {
        bookingFoodAndDrinkService.createBookingFoodAndDrink(bookingFoodAndDrinkCreateRequest);
        return ApiResponse.<Void>builder().message("Create bookingF&D finish!").build();
    }
}
