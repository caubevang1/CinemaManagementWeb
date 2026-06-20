package com.cinemaweb.API.Cinema.Web.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    int cinemaId;
    String roomName;
    int numCol;  // 1,2,3,4,5,...
    int numRow; // 1-A, 2-B, 3-C, 4-D, 5-E,...

    double[] seatPrice = new double[3];  //"Couple", "Normal", "VIP"
}
