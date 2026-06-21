package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleResponse {
    int scheduleId;
    int movieId;
    int roomId;
    int cinemaId;   // suy ra từ room.cinema (giữ trong response cho FE)
    String movieName;
    String roomName;
    String cinemaName;

    LocalDateTime scheduleStart;
    LocalDateTime scheduleEnd;

    String format;
    String audioType;
}
