package com.cinemaweb.API.Cinema.Web.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionRequest {
    @NotNull(message = "PERMISSION_NAME_NULL")
    String name;
    String description;
}
