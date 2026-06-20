package com.cinemaweb.API.Cinema.Web.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodAndDrinkRequest {
    String foodAndDrinkName;
    String cinemaId;
    double foodAndDrinkPrice;
    String imageFoodAndDrink;
}
