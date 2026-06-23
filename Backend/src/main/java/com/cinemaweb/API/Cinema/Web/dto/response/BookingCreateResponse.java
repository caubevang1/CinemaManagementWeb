package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreateResponse {
    int bookingId;
    // URL VNPay để frontend redirect người dùng sang thanh toán.
    String paymentUrl;
}
