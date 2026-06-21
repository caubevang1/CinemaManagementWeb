package com.cinemaweb.API.Cinema.Web.dto.response;

import com.cinemaweb.API.Cinema.Web.enums.FriendshipStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendshipResponse {
    Integer friendshipId;
    FriendshipStatus status;
    LocalDateTime createdAt;

    // Người ở "phía bên kia" so với người dùng đang đăng nhập
    String otherUserId;
    String otherUsername;
    String otherAvatar;
}
