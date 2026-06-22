package com.cinemaweb.API.Cinema.Web.dto.response;

import com.cinemaweb.API.Cinema.Web.enums.TicketTransferStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payload tự chứa đủ thông tin để hiển thị thẻ lời mời chuyển vé (offer card) ở FE.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketTransferResponse {
    Integer id;
    int bookingId;

    String fromUserId;
    String fromUsername;
    String toUserId;
    String toUsername;

    String movieName;
    String cinemaName;
    LocalDateTime scheduleStart;
    List<String> seats;   // nhãn ghế, vd ["A5", "A6"]

    TicketTransferStatus status;
    LocalDateTime createdAt;
}
