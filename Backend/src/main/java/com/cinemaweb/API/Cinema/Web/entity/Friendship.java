package com.cinemaweb.API.Cinema.Web.entity;

import com.cinemaweb.API.Cinema.Web.enums.FriendshipStatus;
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
@Table(name = "friendship", uniqueConstraints = {
        @UniqueConstraint(name = "uq_friendship_pair", columnNames = {"requester_id", "addressee_id"})
})
public class Friendship {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "friendship_id")
    Integer friendshipId;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    User requester;

    @ManyToOne
    @JoinColumn(name = "addressee_id")
    User addressee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    FriendshipStatus status;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "responded_at")
    LocalDateTime respondedAt;
}
