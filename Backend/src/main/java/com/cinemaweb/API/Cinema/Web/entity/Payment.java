package com.cinemaweb.API.Cinema.Web.entity;

import com.cinemaweb.API.Cinema.Web.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_booking", columnList = "booking_id")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "payment_id")
    Integer paymentId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    // Mã giao dịch gửi sang VNPay (vnp_TxnRef), dùng để tra ngược khi callback.
    @Column(name = "txn_ref")
    String txnRef;

    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    PaymentStatus status;

    String method;

    @Column(name = "bank_code")
    String bankCode;

    @Column(name = "vnp_transaction_no")
    String vnpTransactionNo;

    @Column(name = "response_code")
    String responseCode;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "paid_at")
    LocalDateTime paidAt;
}
