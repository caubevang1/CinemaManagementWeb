package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.FoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.FoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.entity.FoodAndDrink;
import com.cinemaweb.API.Cinema.Web.mapper.FoodAndDrinkMapper;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import com.cinemaweb.API.Cinema.Web.repository.FoodAndDrinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodAndDrinkService {
    @Autowired
    private FoodAndDrinkRepository foodAndDrinkRepository;

    @Autowired
    private FoodAndDrinkMapper foodAndDrinkMapper;

    @Autowired
    private CinemaRepository cinemaRepository;

    public List<FoodAndDrinkResponse> getAllFoodAndDrink() {
        return foodAndDrinkMapper.toFoodAndDrinkResponseList(foodAndDrinkRepository.findAll());
    }

    public FoodAndDrinkResponse getFoodAndDrink(String foodAndDrinkId) {
        return foodAndDrinkMapper.toFoodAndDrinkResponse(foodAndDrinkRepository.findById(foodAndDrinkId)
                .orElseThrow(() -> new RuntimeException("FoodAndDrink id is not found")));
    }

    public void createFoodAndDrink(FoodAndDrinkRequest foodAndDrinkCreateRequest) {
        FoodAndDrink fd = foodAndDrinkMapper.toCreateFoodAndDrink(foodAndDrinkCreateRequest);
        foodAndDrinkRepository.save(fd);
    }

    public void updateFoodAndDrink(String foodAndDrinkId, FoodAndDrinkRequest foodAndDrinkUpdateRequest) {
        Cinema cinema = cinemaRepository.findById(foodAndDrinkUpdateRequest.getCinemaId())
                .orElseThrow(() -> new RuntimeException("Cinema id in update fd is not found!"));

        FoodAndDrink foodAndDrink = foodAndDrinkRepository.findById(foodAndDrinkId)
                .orElseThrow(() -> new RuntimeException("FoodAndDrink id in update fd is not found!"));
        foodAndDrink.setCinema(cinema);
        foodAndDrinkMapper.toUpdateFoodAndDrink(foodAndDrink,foodAndDrinkUpdateRequest);

        foodAndDrinkRepository.save(foodAndDrink);
    }

    public void deleteFoodAndDrink(String foodAndDrinkId) {
        foodAndDrinkRepository.deleteById(foodAndDrinkId);
    }
}
