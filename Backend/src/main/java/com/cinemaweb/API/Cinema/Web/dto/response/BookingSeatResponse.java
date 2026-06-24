package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingSeatResponse {
    int id;
    int bookingId;
    int seatScheduleId;
    String seatLabel;
    BigDecimal price;
}
