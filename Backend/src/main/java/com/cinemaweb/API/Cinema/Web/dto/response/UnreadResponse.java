package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnreadResponse {
    long total;
    Map<String, Long> byFriend;
}
