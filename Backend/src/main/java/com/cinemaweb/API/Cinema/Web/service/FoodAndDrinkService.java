package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.FoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.FoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.entity.FoodAndDrink;
import com.cinemaweb.API.Cinema.Web.configuration.CacheConfig;
import com.cinemaweb.API.Cinema.Web.mapper.FoodAndDrinkMapper;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import com.cinemaweb.API.Cinema.Web.repository.FoodAndDrinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Cacheable(value = CacheConfig.FOODS)
    public List<FoodAndDrinkResponse> getAllFoodAndDrink() {
        return foodAndDrinkMapper.toFoodAndDrinkResponseList(foodAndDrinkRepository.findAll());
    }

    @Cacheable(value = CacheConfig.FOOD, key = "#foodAndDrinkId")
    public FoodAndDrinkResponse getFoodAndDrink(String foodAndDrinkId) {
        return foodAndDrinkMapper.toFoodAndDrinkResponse(foodAndDrinkRepository.findById(foodAndDrinkId)
                .orElseThrow(() -> new RuntimeException("FoodAndDrink id is not found")));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.FOODS, allEntries = true),
            @CacheEvict(value = CacheConfig.FOOD, allEntries = true)
    })
    public void createFoodAndDrink(FoodAndDrinkRequest foodAndDrinkCreateRequest) {
        FoodAndDrink fd = foodAndDrinkMapper.toCreateFoodAndDrink(foodAndDrinkCreateRequest);
        foodAndDrinkRepository.save(fd);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.FOODS, allEntries = true),
            @CacheEvict(value = CacheConfig.FOOD, key = "#foodAndDrinkId")
    })
    public void updateFoodAndDrink(String foodAndDrinkId, FoodAndDrinkRequest foodAndDrinkUpdateRequest) {
        Cinema cinema = cinemaRepository.findById(foodAndDrinkUpdateRequest.getCinemaId())
                .orElseThrow(() -> new RuntimeException("Cinema id in update fd is not found!"));

        FoodAndDrink foodAndDrink = foodAndDrinkRepository.findById(foodAndDrinkId)
                .orElseThrow(() -> new RuntimeException("FoodAndDrink id in update fd is not found!"));
        String oldImage = foodAndDrink.getImageFoodAndDrink();
        foodAndDrink.setCinema(cinema);
        foodAndDrinkMapper.toUpdateFoodAndDrink(foodAndDrink,foodAndDrinkUpdateRequest);

        foodAndDrinkRepository.save(foodAndDrink);
        // Ảnh đã đổi -> xóa ảnh cũ trên Cloudinary (best-effort)
        if (foodAndDrink.getImageFoodAndDrink() != null
                && !foodAndDrink.getImageFoodAndDrink().equals(oldImage))
            cloudinaryService.deleteByUrl(oldImage);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.FOODS, allEntries = true),
            @CacheEvict(value = CacheConfig.FOOD, key = "#foodAndDrinkId")
    })
    public void deleteFoodAndDrink(String foodAndDrinkId) {
        foodAndDrinkRepository.findById(foodAndDrinkId)
                .ifPresent(fd -> cloudinaryService.deleteByUrl(fd.getImageFoodAndDrink()));
        foodAndDrinkRepository.deleteById(foodAndDrinkId);
    }
}
