package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodAndDrinkResponse {
    int foodAndDrinkId;
    String foodAndDrinkName;
    int cinemaId;
    String cinemaName;
    BigDecimal foodAndDrinkPrice;
    String imageFoodAndDrink;
}
