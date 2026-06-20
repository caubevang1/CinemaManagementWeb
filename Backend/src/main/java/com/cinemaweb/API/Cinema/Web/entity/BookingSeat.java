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
@Table(name = "booking_seat")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "seat_schedule_id")
    SeatSchedule seatSchedule;

    double price;
}
