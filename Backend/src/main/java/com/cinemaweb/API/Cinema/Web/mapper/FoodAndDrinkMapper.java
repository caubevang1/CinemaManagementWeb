package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.FoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.FoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.entity.FoodAndDrink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodAndDrinkMapper {
    @Mapping(source = "cinema.cinemaName", target = "cinemaName")
    FoodAndDrinkResponse toFoodAndDrinkResponse(FoodAndDrink foodAndDrink);

    @Mapping(source = "cinema.cinemaName", target = "cinemaName")
    List<FoodAndDrinkResponse> toFoodAndDrinkResponseList(List<FoodAndDrink> foodAndDrinkList);

    @Mapping(source = "cinemaId", target = "cinema.cinemaId")
    FoodAndDrink toCreateFoodAndDrink(FoodAndDrinkRequest FoodAndDrinkCreateRequest);

    @Mapping(target = "cinema", ignore = true)
    void toUpdateFoodAndDrink(@MappingTarget FoodAndDrink foodAndDrink, FoodAndDrinkRequest FoodAndDrinkUpdateRequest);
}
