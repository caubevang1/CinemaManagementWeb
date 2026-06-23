package com.cinemaweb.API.Cinema.Web.messaging;

import com.cinemaweb.API.Cinema.Web.configuration.RabbitConfig;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.event.BookingPaidEvent;
import com.cinemaweb.API.Cinema.Web.service.BookingService;
import com.cinemaweb.API.Cinema.Web.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Worker xử lý side-effect sau thanh toán: gửi email vé. Chạy ngoài luồng IPN/return nên SMTP
 * chậm hay lỗi không làm VNPay timeout, cũng không rollback đơn đã PAID.
 *
 * Nếu ném exception -> listener container retry theo cấu hình (spring.rabbitmq.listener.simple.retry);
 * cạn lượt -> message bị reject -> rơi vào dead-letter queue (booking.paid.dlq) để kiểm tra/replay.
 *
 * @Transactional(readOnly): mở session JPA cho getBooking() nạp các quan hệ lazy (movie/cinema/room)
 * — luồng web có OSIV lo việc này, còn thread của listener thì không.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingPaidListener {

    BookingService bookingService;
    EmailService emailService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional(readOnly = true)
    public void onBookingPaid(BookingPaidEvent event) {
        log.info("Worker received BookingPaidEvent bookingId={}", event.bookingId());
        BookingResponse details = bookingService.getBooking(String.valueOf(event.bookingId()));
        emailService.sendTicketEmail(event.email(), details);
    }
}
