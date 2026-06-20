package com.cinemaweb.API.Cinema.Web.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int seatId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    Room room;

    String seatType;
    char seatRow;
    int seatNumber;
    double seatPrice;
}
