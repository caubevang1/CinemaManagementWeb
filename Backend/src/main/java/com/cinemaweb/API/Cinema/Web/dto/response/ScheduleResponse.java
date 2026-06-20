package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleResponse {
    int scheduleId;
    int movieId;
    int roomId;
    int cinemaId;
    String movieName;
    String roomName;
    String cinemaName;

    LocalDate scheduleDate;  //LocalDate in Jav
    LocalTime scheduleStart; //LocalTime in Java
    LocalTime scheduleEnd;
}
