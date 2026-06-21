package com.cinemaweb.API.Cinema.Web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendRequestDto {
    @NotBlank(message = "addresseeId không được để trống!")
    String addresseeId;
}
