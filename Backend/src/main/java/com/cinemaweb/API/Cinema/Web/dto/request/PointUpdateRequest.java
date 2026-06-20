package com.cinemaweb.API.Cinema.Web.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PointUpdateRequest {
    @NotNull(message = "EMAIL_IS_NULL")
    String email;
    @NotNull(message = "POINT_IS_NULL")
    double point;
}
