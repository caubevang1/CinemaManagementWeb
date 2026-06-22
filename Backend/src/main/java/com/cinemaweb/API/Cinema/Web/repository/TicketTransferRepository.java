package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.TicketTransfer;
import com.cinemaweb.API.Cinema.Web.enums.TicketTransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTransferRepository extends JpaRepository<TicketTransfer, Integer> {

    // Chặn nhiều lời mời PENDING trên cùng một vé.
    boolean existsByBooking_BookingIdAndStatus(int bookingId, TicketTransferStatus status);

    // Lời mời đến đang chờ xử lý.
    List<TicketTransfer> findByToUser_IDAndStatus(String toUserId, TicketTransferStatus status);

    // Lời mời đã gửi đang chờ xử lý.
    List<TicketTransfer> findByFromUser_IDAndStatus(String fromUserId, TicketTransferStatus status);
}
