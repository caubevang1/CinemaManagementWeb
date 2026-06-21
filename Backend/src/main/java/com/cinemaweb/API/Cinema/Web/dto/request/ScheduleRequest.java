package com.cinemaweb.API.Cinema.Web.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleRequest {
    int movieId;
    int roomId;
    // cinemaId bỏ khỏi request — rạp suy ra từ room.cinema.

    LocalDateTime scheduleStart; // DATETIME (ngày + giờ)
    LocalDateTime scheduleEnd;

    String format;     // 2D / 3D / IMAX
    String audioType;  // SUBTITLE / DUB
}
