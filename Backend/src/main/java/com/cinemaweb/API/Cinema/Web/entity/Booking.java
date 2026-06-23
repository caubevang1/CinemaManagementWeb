package com.cinemaweb.API.Cinema.Web.entity;

import com.cinemaweb.API.Cinema.Web.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.LocalDateTime.now;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int bookingId;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    BigDecimal price;
    LocalDateTime bookingDay;

    // Hạn chót giữ ghế cho đơn PENDING (= heldUntil lúc giữ ghế). Cron huỷ đơn quá hạn theo
    // mốc này, và VNPay nhận đúng mốc qua vnp_ExpireDate -> một đồng hồ đếm ngược duy nhất.
    @Column(name = "expires_at")
    LocalDateTime expiresAt;

    // PENDING khi mới tạo (chờ thanh toán) -> PAID khi thanh toán thành công -> CANCELLED nếu huỷ/hết hạn.
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    BookingStatus status;
}
