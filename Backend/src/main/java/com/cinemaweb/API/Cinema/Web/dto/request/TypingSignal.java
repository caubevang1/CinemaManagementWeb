package com.cinemaweb.API.Cinema.Web.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Tín hiệu "đang nhập". Khi gửi lên: recipientId là người nhận tín hiệu.
 * Khi chuyển tiếp xuống FE: fromUserId là người đang gõ.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypingSignal {
    String recipientId;
    String fromUserId;
    boolean typing;
}
