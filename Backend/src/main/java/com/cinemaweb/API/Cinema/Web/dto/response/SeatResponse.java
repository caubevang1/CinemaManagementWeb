package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatResponse {
    int seatId;
    int roomId;
    String roomName;
    String seatType;
    String seatRow;
    int seatNumber;
    double seatPrice;
}
