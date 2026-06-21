package com.cinemaweb.API.Cinema.Web.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatHoldRequest {
    // Danh sách seat_schedule_id muốn giữ tạm / nhả.
    List<Integer> seatScheduleIds;
}
