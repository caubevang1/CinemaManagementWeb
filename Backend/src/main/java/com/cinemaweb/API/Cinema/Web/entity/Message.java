package com.cinemaweb.API.Cinema.Web.entity;

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
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chat_pair", columnList = "sender_id, recipient_id, sent_at"),
        @Index(name = "idx_chat_unread", columnList = "recipient_id, read_at")
})
public class Message {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "message_id")
    Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    User recipient;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    // "TEXT" (tin thường) hoặc "TRANSFER" (lời mời chuyển nhượng vé).
    @Column(name = "type")
    String type;

    // Khi type = TRANSFER: id của TicketTransfer liên kết.
    @Column(name = "transfer_id")
    Integer transferId;

    @Column(name = "sent_at")
    LocalDateTime sentAt;

    @Column(name = "read_at")
    LocalDateTime readAt;
}
