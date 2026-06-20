package com.cinemaweb.API.Cinema.Web.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatSchedule {
    @Column(name = "seat_schedule_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int seatScheduleId;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    Seat seat;

    boolean seatState; // true = booked, false = available
}
