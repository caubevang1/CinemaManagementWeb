package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Tóm tắt một cuộc trò chuyện cho danh sách trong widget chat.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationSummaryResponse {
    String userId;
    String username;
    String avatar;
    String lastMessage;
    LocalDateTime lastMessageAt;
    long unreadCount;
    boolean online;
}
