package com.cinemaweb.API.Cinema.Web.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleRequest {
    int movieId;
    int roomId;
    int cinemaId;

    LocalDate scheduleDate;  //LocalDate in Java
    LocalTime scheduleStart; //LocalTime in Java
    LocalTime scheduleEnd;
}
