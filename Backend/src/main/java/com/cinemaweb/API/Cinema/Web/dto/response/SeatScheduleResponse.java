package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatScheduleResponse {
    int seatScheduleId;
    int scheduleId;
    int seatId;
    String seatType;
    char seatRow;
    int seatNumber;
    BigDecimal seatPrice;        // giá hiệu lực theo suất (seat_schedule.price)
    String seatState;            // AVAILABLE / HELD / BOOKED
    LocalDateTime heldUntil;     // thời điểm hết hạn giữ ghế (khi HELD)
}
