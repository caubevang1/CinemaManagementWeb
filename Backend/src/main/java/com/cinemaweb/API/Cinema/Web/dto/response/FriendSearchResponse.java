package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Kết quả tìm bạn: thông tin user kèm trạng thái quan hệ với người đang tìm
 * (NONE / PENDING). Bạn bè đã ACCEPTED bị loại khỏi kết quả.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendSearchResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    String avatar;
    String email;
    String friendshipStatus; // "NONE" | "PENDING"
}
