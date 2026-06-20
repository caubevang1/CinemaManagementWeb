package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodAndDrinkResponse {
    int foodAndDrinkId;
    String foodAndDrinkName;
    String cinemaName;
    double foodAndDrinkPrice;
    String imageFoodAndDrink;
}
