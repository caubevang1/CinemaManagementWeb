package com.cinemaweb.API.Cinema.Web.entity;


import com.cinemaweb.API.Cinema.Web.enums.SeatState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

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

    // Trạng thái lưu ở DB chỉ còn AVAILABLE / BOOKED. Trạng thái HELD (giữ tạm) nay nằm
    // hoàn toàn ở Redis (key seat_hold:{id}, TTL tự hết hạn) — xem SeatScheduleService.
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_state")
    SeatState seatState;

    // Chốt chặn optimistic-lock bổ trợ cho pessimistic lock + UNIQUE(booking_seat).
    @Version
    Long version;

    // Giá vé theo từng suất chiếu (khởi tạo từ seat.seatPrice nhưng có thể đổi theo suất).
    BigDecimal price;
}
