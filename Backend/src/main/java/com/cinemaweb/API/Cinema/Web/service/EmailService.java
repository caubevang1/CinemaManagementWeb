package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.entity.User;
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

    // Gửi email xác nhận vé sau khi thanh toán thành công. Được gọi bởi worker RabbitMQ
    // (BookingPaidListener) — tách khỏi luồng thanh toán nên SMTP chậm/lỗi không ảnh hưởng đơn.
    public void sendTicketEmail(String email, BookingResponse booking) throws MailException {
        if (email == null || email.isBlank()) {
            log.warn("Skip ticket email: no recipient for bookingId={}", booking.getBookingId());
            return;
        }
        int seatCount = booking.getSeats() == null ? 0 : booking.getSeats().size();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromAddress);
        message.setSubject("Xác nhận vé xem phim - Đơn #" + booking.getBookingId());
        message.setSentDate(new Date());
        message.setText("Cảm ơn bạn đã đặt vé!\n\n"
                + "Mã đơn: " + booking.getBookingId() + "\n"
                + "Phim: " + booking.getMovieName() + "\n"
                + "Rạp: " + booking.getCinemaName() + "\n"
                + "Phòng: " + booking.getRoomName() + "\n"
                + "Số ghế: " + seatCount + "\n"
                + "Tổng tiền: " + booking.getPrice() + " VND\n\n"
                + "Vui lòng xuất trình email này tại quầy. Chúc bạn xem phim vui vẻ!");
        mailSender.send(message);
        log.info("Sent ticket email bookingId={} to={}", booking.getBookingId(), email);
    }

}
