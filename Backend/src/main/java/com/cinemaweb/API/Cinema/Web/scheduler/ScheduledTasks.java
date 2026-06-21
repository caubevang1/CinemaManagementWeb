package com.cinemaweb.API.Cinema.Web.scheduler;


import com.cinemaweb.API.Cinema.Web.repository.PasswordOtpRepository;
import com.cinemaweb.API.Cinema.Web.repository.SeatScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ScheduledTasks {

    // Token bị thu hồi nay lưu trong Redis và tự hết hạn theo TTL -> không cần dọn thủ công.
    PasswordOtpRepository passwordOtpRepository;

    SeatScheduleRepository seatScheduleRepository;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void passwordOtpCleaning() {
        passwordOtpRepository.deleteAllInvalid();
        log.info("Scheduled task: CLEANING_INVALID_PASSWORD_OTP");
    }

    // Nhả các ghế giữ tạm (HELD) đã quá held_until về AVAILABLE để người khác đặt được.
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void releaseExpiredSeatHolds() {
        int released = seatScheduleRepository.releaseExpiredHolds(LocalDateTime.now());
        if (released > 0) {
            log.info("Scheduled task: RELEASED_EXPIRED_SEAT_HOLDS count={}", released);
        }
    }
}
