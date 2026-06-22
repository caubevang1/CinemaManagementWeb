package com.cinemaweb.API.Cinema.Web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodAndDrinkRequest {
    @NotBlank(message = "Tên combo không được để trống")
    String foodAndDrinkName;
    @NotBlank(message = "Phải chọn rạp cho combo")
    String cinemaId;
    @Positive(message = "Giá combo phải lớn hơn 0")
    double foodAndDrinkPrice;
    String imageFoodAndDrink;
}
