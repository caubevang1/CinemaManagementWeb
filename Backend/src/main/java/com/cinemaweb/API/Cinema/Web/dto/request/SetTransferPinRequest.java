package com.cinemaweb.API.Cinema.Web.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetTransferPinRequest {
    // Bắt buộc khi đã có PIN (để đổi); bỏ trống khi đặt lần đầu.
    String currentPin;

    @Pattern(regexp = "\\d{6}", message = "Mã PIN phải gồm đúng 6 chữ số!")
    String newPin;
}
