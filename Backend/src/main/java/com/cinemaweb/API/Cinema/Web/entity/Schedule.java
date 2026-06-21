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
public class Schedule {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    int scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    Movie movie;

    // Rạp suy ra qua room.cinema — không lưu cinema_id trên schedule nữa (tránh dữ liệu thừa/lệch).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    Room room;

    // Gộp ngày + giờ vào DATETIME để suất chiếu qua nửa đêm (end < start cũ) không còn sai logic.
    LocalDateTime scheduleStart;
    LocalDateTime scheduleEnd;

    // Định dạng chiếu: 2D / 3D / IMAX
    String format;
    // Âm thanh: SUBTITLE (phụ đề) / DUB (lồng tiếng)
    String audioType;
}
