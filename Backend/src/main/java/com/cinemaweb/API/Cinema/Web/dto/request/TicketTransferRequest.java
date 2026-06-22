package com.cinemaweb.API.Cinema.Web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketTransferRequest {
    int bookingId;

    @NotBlank
    String toUserId;

    // Mã PIN chuyển nhượng của người gửi (xác nhận mỗi lần chuyển).
    @Pattern(regexp = "\\d{6}", message = "Mã PIN phải gồm đúng 6 chữ số!")
    String pin;
}
