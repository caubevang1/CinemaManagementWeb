package com.cinemaweb.API.Cinema.Web.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bookingfoodanddrink")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingFoodAndDrink {

    @Column(name = "booking_fd_id")
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int bookingFoodAndDrinkId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "fd_id")
    FoodAndDrink foodAndDrink;

    int quantity;

    double price; // Tổng giá món này (giá * số lượng)
}
