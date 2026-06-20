package com.cinemaweb.API.Cinema.Web.dto.response;


import com.cinemaweb.API.Cinema.Web.entity.BookingFoodAndDrink;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    int bookingId;
    String roomName;
    String cinemaName;
    String movieName;
    List<BookingSeatResponse> seats;
    String username;
    List<BookingFoodAndDrinkResponse> foodAndDrinks;
    Double price;
    LocalDateTime bookingDay;
}
