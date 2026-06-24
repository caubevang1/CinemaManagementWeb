package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingFoodAndDrinkResponse {
    int bookingId;
    int foodAndDrinkId;
    String foodAndDrinkName;
    int quantity;
    BigDecimal price;
}
