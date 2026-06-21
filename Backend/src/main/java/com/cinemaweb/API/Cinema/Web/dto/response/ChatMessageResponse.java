package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    Integer id;
    String senderId;
    String recipientId;
    String content;
    LocalDateTime sentAt;
    LocalDateTime readAt;
}
