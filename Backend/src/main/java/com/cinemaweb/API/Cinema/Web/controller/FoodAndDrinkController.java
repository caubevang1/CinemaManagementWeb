package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.FoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.FoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.service.FoodAndDrinkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foodanddrink")
public class FoodAndDrinkController {
    @Autowired
    private FoodAndDrinkService foodAndDrinkService;

    @GetMapping
    public ApiResponse<List<FoodAndDrinkResponse>> getAllFoodAndDrink() {
        return ApiResponse.<List<FoodAndDrinkResponse>>builder()
                .body(foodAndDrinkService.getAllFoodAndDrink())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<FoodAndDrinkResponse> getFoodAndDrink(@PathVariable String id) {
        return ApiResponse.<FoodAndDrinkResponse>builder()
                .body(foodAndDrinkService.getFoodAndDrink(id))
                .build();
    }

    @PostMapping
    public ApiResponse<Void> createFoodAndDrink(@RequestBody @Valid FoodAndDrinkRequest fdCreateRequest) {
        foodAndDrinkService.createFoodAndDrink(fdCreateRequest);
        return ApiResponse.<Void>builder().message("Food & drink has been created").build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateFoodAndDrink(@RequestBody @Valid FoodAndDrinkRequest fdUpdateRequest, @PathVariable String id) {
        foodAndDrinkService.updateFoodAndDrink(id, fdUpdateRequest);
        return ApiResponse.<Void>builder().message("Food & drink with id " + id + " has been updated").build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteFoodAndDrink(@PathVariable String id) {
        foodAndDrinkService.deleteFoodAndDrink(id);
        return ApiResponse.<Void>builder().message("Food & drink with id " + id + " has been deleted").build();
    }
}
