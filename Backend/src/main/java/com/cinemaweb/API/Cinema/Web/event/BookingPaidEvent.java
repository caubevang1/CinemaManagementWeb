package com.cinemaweb.API.Cinema.Web.event;

import java.io.Serializable;

/**
 * Phát ra khi một đơn chuyển sang PAID (do IPN hoặc return URL xác nhận thanh toán thành công).
 * Dùng cho cả hai mục đích:
 *  - Spring ApplicationEvent: PaymentService publish trong transaction, một
 *    {@code @TransactionalEventListener(AFTER_COMMIT)} mới đẩy sang RabbitMQ (chỉ khi DB đã commit).
 *  - Payload message RabbitMQ: worker BookingPaidListener nhặt ra để gửi email vé.
 * Implements Serializable + dùng kiểu đơn giản để Jackson serialize qua broker dễ dàng.
 */
public record BookingPaidEvent(int bookingId, String txnRef, String email) implements Serializable {
}
