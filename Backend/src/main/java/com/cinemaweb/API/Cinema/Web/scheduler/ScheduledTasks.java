package com.cinemaweb.API.Cinema.Web.scheduler;


import com.cinemaweb.API.Cinema.Web.repository.PasswordOtpRepository;
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

    // Token bị thu hồi nay lưu trong Redis và tự hết hạn theo TTL -> không cần dọn thủ công.
    PasswordOtpRepository passwordOtpRepository;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void passwordOtpCleaning() {
        passwordOtpRepository.deleteAllInvalid();
        log.info("Scheduled task: CLEANING_INVALID_PASSWORD_OTP");
    }
}
