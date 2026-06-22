package com.cinemaweb.API.Cinema.Web.entity;

import com.cinemaweb.API.Cinema.Web.enums.TicketTransferStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "ticket_transfer", indexes = {
        @Index(name = "idx_transfer_recipient", columnList = "to_user_id, status"),
        @Index(name = "idx_transfer_booking", columnList = "booking_id")
})
public class TicketTransfer {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "transfer_id")
    Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "from_user_id")
    User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id")
    User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    TicketTransferStatus status;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "responded_at")
    LocalDateTime respondedAt;
}
