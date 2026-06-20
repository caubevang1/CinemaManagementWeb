package com.cinemaweb.API.Cinema.Web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Table(name = "password_otp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordOTP {
    @Id
    @JsonProperty("OTP")
    String OTP;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @NotNull
    boolean valid;

    @NotNull
    Date expiryTime;
}
