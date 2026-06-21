package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Báo cho người gửi biết các tin nhắn của họ (gửi tới byUserId) đã được đọc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadReceipt {
    String byUserId;
    LocalDateTime readAt;
}
