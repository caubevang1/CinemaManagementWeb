package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingFoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingFoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.entity.BookingFoodAndDrink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingFoodAndDrinkMapper {
    @Mapping(source = "foodAndDrink.foodAndDrinkId", target = "foodAndDrinkId")
    @Mapping(source = "booking.bookingId", target = "bookingId")
    BookingFoodAndDrinkResponse toBookingFoodAndDrink(BookingFoodAndDrink bookingFoodAndDrink);

    @Mapping(source = "foodAndDrink.foodAndDrinkId", target = "foodAndDrinkId")
    @Mapping(source = "booking.bookingId", target = "bookingId")
    List<BookingFoodAndDrinkResponse> toListBookingFoodAndDrinks(List<BookingFoodAndDrink> listBookingFoodAndDrink);

    @Mapping(source = "foodAndDrinkId", target = "foodAndDrink.foodAndDrinkId")
    BookingFoodAndDrink toCreateBookingFoodAndDrink(BookingFoodAndDrinkRequest bookingFoodAndDrinkCreateRequest);
}
