package com.cinemaweb.API.Cinema.Web.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatReadRequest {
    // Người bạn mà ta vừa đọc tin nhắn của họ.
    String friendId;
}
