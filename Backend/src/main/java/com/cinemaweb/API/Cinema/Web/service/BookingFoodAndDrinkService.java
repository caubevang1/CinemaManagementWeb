package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingFoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingFoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.entity.BookingFoodAndDrink;
import com.cinemaweb.API.Cinema.Web.mapper.BookingFoodAndDrinkMapper;
import com.cinemaweb.API.Cinema.Web.repository.BookingFoodAndDrinkRepository;
import com.cinemaweb.API.Cinema.Web.repository.FoodAndDrinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingFoodAndDrinkService {
    @Autowired
    private BookingFoodAndDrinkRepository bookingFoodAndDrinkRepository;

    @Autowired
    private BookingFoodAndDrinkMapper bookingFoodAndDrinkMapper;

    @Autowired
    private FoodAndDrinkRepository foodAndDrinkRepository;

    public BookingFoodAndDrinkResponse getBookingFoodAndDrink(String bookingFoodAndDrinkId) {
        return bookingFoodAndDrinkMapper.toBookingFoodAndDrink(bookingFoodAndDrinkRepository.
                findById(bookingFoodAndDrinkId)
                .orElseThrow(() -> new RuntimeException("BookingF&D is not found !")));
    }

    public List<BookingFoodAndDrinkResponse> getListBookingFoodAndDrink() {
        return bookingFoodAndDrinkMapper.toListBookingFoodAndDrinks(bookingFoodAndDrinkRepository.
                findAll());
    }

    public void createBookingFoodAndDrink(BookingFoodAndDrinkRequest bookingFoodAndDrinkCreateRequest) {
        BookingFoodAndDrink bookingFoodAndDrink =  bookingFoodAndDrinkMapper
                .toCreateBookingFoodAndDrink(bookingFoodAndDrinkCreateRequest);

        double price = bookingFoodAndDrinkCreateRequest.getQuantity() *
                foodAndDrinkRepository.findById(Integer.toString(bookingFoodAndDrinkCreateRequest.getFoodAndDrinkId()))
                        .orElseThrow(() -> new RuntimeException("FoodAndDrinkId in createBookingF&D is not found !"))
                        .getFoodAndDrinkPrice();

        bookingFoodAndDrink.setPrice(price);

        bookingFoodAndDrinkRepository.save(bookingFoodAndDrink);
    }
}
