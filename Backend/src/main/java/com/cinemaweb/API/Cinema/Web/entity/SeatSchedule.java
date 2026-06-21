package com.cinemaweb.API.Cinema.Web.entity;


import com.cinemaweb.API.Cinema.Web.enums.SeatState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatSchedule {
    @Column(name = "seat_schedule_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int seatScheduleId;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    Seat seat;

    // AVAILABLE / HELD / BOOKED (thay cho boolean cũ).
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_state")
    SeatState seatState;

    // Hết hạn giữ ghế tạm: khi seatState = HELD, sau mốc này cron sẽ tự nhả về AVAILABLE.
    LocalDateTime heldUntil;

    // Ai đang giữ ghế (để chỉ người giữ mới được đặt tiếp).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by_user_id")
    User heldBy;

    // Chốt chặn optimistic-lock bổ trợ cho pessimistic lock + UNIQUE(booking_seat).
    @Version
    Long version;

    // Giá vé theo từng suất chiếu (khởi tạo từ seat.seatPrice nhưng có thể đổi theo suất).
    BigDecimal price;
}
