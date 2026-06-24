package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingSeatResponse;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender mailSender;

    // Địa chỉ người gửi (From). Với Brevo, đây PHẢI là sender đã verify trong tài khoản — khác với
    // SMTP login. Cấu hình qua MAIL_FROM để không hardcode. @NonFinal vì Spring set sau constructor.
    @Value("${mail.from}")
    @NonFinal
    String fromAddress;

    @CircuitBreaker(name = "email", fallbackMethod = "sendResetPasswordOtpFallback")
    public void sendResetPasswordOtp(User user, String otpToken) throws MailException {
        String resetLink = "http://localhost:5173/reset-password/" + otpToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(fromAddress);
        message.setSubject("Password Reset Token");
        message.setSentDate(new Date());
        message.setText("Hello " + user.getLastName() + "!\n"
                + "Click here to reset your password: " + resetLink + "\n"
                + "Keep it secret! Don't share to anyone!");

        mailSender.send(message);
    }

    private void sendResetPasswordOtpFallback(User user, String otpToken, Throwable t) {
        log.warn("Email circuit open for sendResetPasswordOtp user={}: {}", user.getUsername(), t.getMessage());
        throw new AppException(ErrorCode.EMAIL_SERVICE_UNAVAILABLE);
    }

    // Gửi email xác nhận vé sau khi thanh toán thành công. Được gọi bởi worker RabbitMQ
    // (BookingPaidListener) — tách khỏi luồng thanh toán nên SMTP chậm/lỗi không ảnh hưởng đơn.
    public void sendTicketEmail(String email, BookingResponse booking) throws MailException {
        if (email == null || email.isBlank()) {
            log.warn("Skip ticket email: no recipient for bookingId={}", booking.getBookingId());
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromAddress);
        message.setSubject("Xác nhận vé xem phim - Đơn #" + booking.getBookingId());
        message.setSentDate(new Date());
        message.setText("Cảm ơn bạn đã đặt vé!\n\n"
                + "Mã đơn: " + booking.getBookingId() + "\n"
                + "Phim: " + booking.getMovieName() + "\n"
                + "Rạp: " + booking.getCinemaName() + "\n"
                + "Địa chỉ: " + booking.getCinemaAddress() + "\n"
                + "Phòng: " + booking.getRoomName() + "\n"
                + "Ghế: " + formatSeats(booking) + "\n"
                + "Đồ ăn/thức uống:\n" + formatFoodAndDrinks(booking) + "\n"
                + "Tổng tiền: " + booking.getPrice() + " VND\n\n"
                + "Vui lòng xuất trình email này tại quầy. Chúc bạn xem phim vui vẻ!");
        mailSender.send(message);
        log.info("Sent ticket email bookingId={} to={}", booking.getBookingId(), email);
    }

    // Danh sách ghế thật (vd "A1, A2, B5"), sort cho gọn. "Không" nếu trống.
    private String formatSeats(BookingResponse booking) {
        if (booking.getSeats() == null || booking.getSeats().isEmpty()) {
            return "Không";
        }
        return booking.getSeats().stream()
                .map(BookingSeatResponse::getSeatLabel)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    // Danh sách đồ ăn/thức uống đầy đủ (tên x số lượng - giá), mỗi món một dòng. "Không" nếu trống.
    private String formatFoodAndDrinks(BookingResponse booking) {
        if (booking.getFoodAndDrinks() == null || booking.getFoodAndDrinks().isEmpty()) {
            return "Không";
        }
        return booking.getFoodAndDrinks().stream()
                .map(f -> "- " + f.getFoodAndDrinkName() + " x" + f.getQuantity()
                        + " - " + f.getPrice() + " VND")
                .collect(Collectors.joining("\n"));
    }

}
