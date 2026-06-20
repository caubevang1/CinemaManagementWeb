package com.cinemaweb.API.Cinema.Web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotNull(message = "USERNAME_IS_NULL")
    String username;
    @NotNull(message = "PASSWORD_IS_NULL")
    String password;
}
