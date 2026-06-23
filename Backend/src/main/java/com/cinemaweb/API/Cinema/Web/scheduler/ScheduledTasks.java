package com.cinemaweb.API.Cinema.Web.scheduler;


import com.cinemaweb.API.Cinema.Web.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ScheduledTasks {

    // Giữ ghế tạm (HELD) nay nằm ở Redis với TTL tự hết hạn -> không cần cron nhả ghế nữa.
    // OTP/token thu hồi cũng lưu Redis tự hết hạn. Chỉ còn dọn đơn PENDING bỏ ngang ở VNPay.
    BookingService bookingService;

    // Huỷ các đơn PENDING quá hạn giữ ghế (user bỏ ngang thanh toán) và nhả ghế về AVAILABLE.
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void cancelExpiredPendingBookings() {
        int cancelled = bookingService.cancelExpiredPendingBookings();
        if (cancelled > 0) {
            log.info("Scheduled task: CANCELLED_EXPIRED_PENDING_BOOKINGS count={}", cancelled);
        }
    }
}
