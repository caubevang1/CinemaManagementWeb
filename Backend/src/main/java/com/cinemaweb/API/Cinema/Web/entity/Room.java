package com.cinemaweb.API.Cinema.Web.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Room {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int roomId;

    @ManyToOne
    @JoinColumn(name = "cinema_id")
    Cinema cinema;

    String roomName;

    int numCol;  // 1,2,3,4,5,...
    int numRow; // 1-A, 2-B, 3-C, 4-D, 5-E,...
}
