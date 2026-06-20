package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingFoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.BookingRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.BookingSeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingFoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatResponse;
import com.cinemaweb.API.Cinema.Web.entity.*;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.BookingFoodAndDrinkMapper;
import com.cinemaweb.API.Cinema.Web.mapper.BookingMapper;
import com.cinemaweb.API.Cinema.Web.mapper.BookingSeatMapper;
import com.cinemaweb.API.Cinema.Web.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private BookingFoodAndDrinkRepository bookingFoodAndDrinkRepository;

    @Autowired
    private BookingFoodAndDrinkMapper bookingFoodAndDrinkMapper;

    @Autowired
    private BookingSeatMapper bookingSeatMapper;

    @Autowired
    private SeatScheduleRepository seatScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodAndDrinkRepository foodAndDrinkRepository;

    public BookingResponse getBooking(String bookingId) {
        int bookingIdInt = Integer.parseInt(bookingId);
        List<BookingFoodAndDrinkResponse> listBookingFoodAndDrinks = new ArrayList<>();
        if (bookingFoodAndDrinkRepository.existsByBooking_BookingId(bookingIdInt)) {
            listBookingFoodAndDrinks = bookingFoodAndDrinkMapper.toListBookingFoodAndDrinks(
                    bookingFoodAndDrinkRepository.findByBooking_BookingId(bookingIdInt));
        }
        BookingResponse booking = bookingMapper.toBookingResponse(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking id is not found")));
        // Lấy về các seat của booking
        List<BookingSeat> seats = bookingSeatRepository.findAllByBooking_BookingId(bookingIdInt)
                .orElseThrow(() -> new RuntimeException("Invalid seats"));
        booking.setFoodAndDrinks(listBookingFoodAndDrinks);
        booking.setSeats(seats.stream().map(bookingSeatMapper::toBookingSeatResponse).toList());
        return booking;
    }



    public List<BookingResponse> getAllMyBooking() {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        var bookings =  bookingRepository.findAllByUser_ID(userId)
                .orElseThrow(() -> new RuntimeException("User chua tung co hoa don nao!"));
        List<Integer> bookingIds = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingIds.add(booking.getBookingId());
        }

        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (Integer bookingId : bookingIds) {
            bookingResponses.add(getBooking(Integer.toString(bookingId)));
        }
        return bookingResponses;
    }

    public void createBooking(BookingRequest bookingRequest) {
        Booking booking = bookingMapper.toCreationBooking(bookingRequest);
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        booking.setUser(user);
        bookingRepository.save(booking);
        // Tinh tien seat

        int bookingId = booking.getBookingId();

        double seatPrice = 0;
        List<BookingSeatRequest> bookingSeats = bookingRequest.getSeats();
        List<SeatSchedule> seatSchedules = new ArrayList<>();
        for (int i = 0; i < bookingSeats.size(); i++) {
            SeatSchedule seatSchedule =  seatScheduleRepository
                    .findBySeatScheduleId(bookingSeats.get(i).getSeatScheduleId());
            seatSchedule.setSeatState(true);
            seatSchedules.add(seatSchedule);
            seatPrice += seatSchedule.getSeat().getSeatPrice();

            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .seatSchedule(seatSchedule)
                    .price(seatSchedule.getSeat().getSeatPrice())
                    .build();
            bookingSeatRepository.save(bookingSeat);
        }
        seatScheduleRepository.saveAll(seatSchedules);

        double foodAndDrinksPrice = 0;
        if(bookingRequest.getFoodAndDrinks() != null) {
            List<BookingFoodAndDrinkRequest> listBookingFoodAndDrink =
                    bookingRequest.getFoodAndDrinks();

            for (int i = 0; i < listBookingFoodAndDrink.size(); i++ ) {
                FoodAndDrink foodAndDrink = foodAndDrinkRepository
                        .findById(String.valueOf(listBookingFoodAndDrink.get(i).getFoodAndDrinkId()))
                        .orElseThrow(() -> new RuntimeException("F&D id is not found"));
                foodAndDrinksPrice += listBookingFoodAndDrink.get(i)
                        .getQuantity() * foodAndDrink.getFoodAndDrinkPrice();

                BookingFoodAndDrink bookingFoodAndDrink = BookingFoodAndDrink.builder()
                        .foodAndDrink(foodAndDrink)
                        .booking(booking)
                        .quantity(listBookingFoodAndDrink.get(i).getQuantity())
                        .price(listBookingFoodAndDrink.get(i)
                                .getQuantity() * foodAndDrink.getFoodAndDrinkPrice())
                        .build();

                bookingFoodAndDrinkRepository.save(bookingFoodAndDrink);
            }
        }

        booking.setPrice(seatPrice + foodAndDrinksPrice);
        bookingRepository.save(booking);
    }
}
