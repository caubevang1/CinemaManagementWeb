package com.cinemaweb.API.Cinema.Web.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String ID;
    String username;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    String email;
    String phoneNumber;
    int gender;
    String avatar;
    double point;
    Set<RoleResponse> roles;
}
