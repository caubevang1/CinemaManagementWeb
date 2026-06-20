package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.BookingFoodAndDrink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingFoodAndDrinkRepository extends JpaRepository<BookingFoodAndDrink, String> {
    List<BookingFoodAndDrink> findByBooking_BookingId(int bookingId);

    boolean existsByBooking_BookingId(int bookingId);
}
