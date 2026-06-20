package com.cinemaweb.API.Cinema.Web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetRequest {

    @Size(min = 8, message = "INVALID_PASSWORD")
    @NotNull(message = "PASSWORD_IS_NULL")
    String newPassword;

    @Size(min = 8, message = "INVALID_PASSWORD")
    @NotNull(message = "PASSWORD_IS_NULL")
    String confirmPassword;
}
