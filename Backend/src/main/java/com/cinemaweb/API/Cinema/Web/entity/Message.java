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

    @Column(name = "sent_at")
    LocalDateTime sentAt;

    @Column(name = "read_at")
    LocalDateTime readAt;
}
