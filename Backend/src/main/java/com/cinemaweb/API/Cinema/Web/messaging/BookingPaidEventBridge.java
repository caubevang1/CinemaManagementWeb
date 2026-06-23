package com.cinemaweb.API.Cinema.Web.messaging;

import com.cinemaweb.API.Cinema.Web.configuration.RabbitConfig;
import com.cinemaweb.API.Cinema.Web.event.BookingPaidEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Cầu nối giữa domain event (Spring) và RabbitMQ. PaymentService publish BookingPaidEvent NGAY
 * TRONG transaction thanh toán; bridge này chỉ đẩy message sang broker SAU KHI transaction commit
 * thành công (AFTER_COMMIT) -> không bao giờ gửi email cho đơn bị rollback.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingPaidEventBridge {

    RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingPaid(BookingPaidEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
        log.info("Published BookingPaidEvent to RabbitMQ bookingId={} txnRef={}",
                event.bookingId(), event.txnRef());
    }
}
