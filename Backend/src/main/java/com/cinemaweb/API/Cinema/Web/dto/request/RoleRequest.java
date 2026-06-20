package com.cinemaweb.API.Cinema.Web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
    @NotNull(message = "ROLE_NAME_NULL")
    String name;
    String description;
    Set<String> permissions;
}
