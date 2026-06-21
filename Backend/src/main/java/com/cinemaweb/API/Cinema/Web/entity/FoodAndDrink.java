package com.cinemaweb.API.Cinema.Web.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "foodanddrink")
public class FoodAndDrink {
    @Column(name = "fd_id")
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int foodAndDrinkId;

    @Column(name = "fd_name")
    String foodAndDrinkName;

    @ManyToOne
    @JoinColumn(name = "cinema_id")
    Cinema cinema;

    @Column(name = "fd_price")
    BigDecimal foodAndDrinkPrice;

    @Column(name = "image_food_and_drink")
    String imageFoodAndDrink;
}
