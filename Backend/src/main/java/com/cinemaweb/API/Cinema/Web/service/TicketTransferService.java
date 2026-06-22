package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.TicketTransferRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.TicketTransferResponse;
import com.cinemaweb.API.Cinema.Web.entity.Booking;
import com.cinemaweb.API.Cinema.Web.entity.Seat;
import com.cinemaweb.API.Cinema.Web.entity.TicketTransfer;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.enums.TicketTransferStatus;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.repository.BookingRepository;
import com.cinemaweb.API.Cinema.Web.repository.BookingSeatRepository;
import com.cinemaweb.API.Cinema.Web.repository.FriendshipRepository;
import com.cinemaweb.API.Cinema.Web.repository.TicketTransferRepository;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketTransferService {

    @Autowired
    private TicketTransferRepository transferRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Tạo lời mời chuyển nhượng vé (PENDING) sau khi kiểm tra mọi ràng buộc. */
    @Transactional
    public TicketTransferResponse offer(String fromUserId, TicketTransferRequest request) {
        Booking booking = bookingRepository.findById(String.valueOf(request.getBookingId()))
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_TRANSFER_NOT_FOUND));

        if (booking.getUser() == null || !booking.getUser().getID().equals(fromUserId))
            throw new AppException(ErrorCode.TICKET_NOT_OWNER);

        if (fromUserId.equals(request.getToUserId()))
            throw new AppException(ErrorCode.TICKET_TRANSFER_SELF);

        if (!friendshipRepository.existsAcceptedBetween(fromUserId, request.getToUserId()))
            throw new AppException(ErrorCode.TICKET_TRANSFER_NOT_FRIENDS);

        if (booking.getSchedule() == null
                || booking.getSchedule().getScheduleStart() == null
                || !booking.getSchedule().getScheduleStart().isAfter(LocalDateTime.now()))
            throw new AppException(ErrorCode.TICKET_SHOW_STARTED);

        if (transferRepository.existsByBooking_BookingIdAndStatus(
                booking.getBookingId(), TicketTransferStatus.PENDING))
            throw new AppException(ErrorCode.TICKET_TRANSFER_EXISTS);

        // Xác nhận mã PIN chuyển nhượng của người gửi (kiểu ngân hàng).
        String storedPin = booking.getUser().getTransferPin();
        if (storedPin == null)
            throw new AppException(ErrorCode.TICKET_PIN_NOT_SET);
        if (request.getPin() == null || !passwordEncoder.matches(request.getPin(), storedPin))
            throw new AppException(ErrorCode.TICKET_PIN_INVALID);

        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        TicketTransfer transfer = TicketTransfer.builder()
                .booking(booking)
                .fromUser(booking.getUser())
                .toUser(toUser)
                .status(TicketTransferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(transferRepository.save(transfer));
    }

    /** Người nhận chấp nhận: chuyển quyền sở hữu vé. */
    @Transactional
    public TicketTransferResponse accept(int transferId, String me) {
        TicketTransfer transfer = getPendingForRecipient(transferId, me);

        Booking booking = transfer.getBooking();
        // Tái kiểm tra: suất chiếu vẫn còn ở tương lai và người gửi vẫn sở hữu vé.
        if (booking.getSchedule() == null
                || booking.getSchedule().getScheduleStart() == null
                || !booking.getSchedule().getScheduleStart().isAfter(LocalDateTime.now()))
            throw new AppException(ErrorCode.TICKET_SHOW_STARTED);

        if (booking.getUser() == null || !booking.getUser().getID().equals(transfer.getFromUser().getID()))
            throw new AppException(ErrorCode.TICKET_NOT_OWNER);

        booking.setUser(transfer.getToUser());
        bookingRepository.save(booking);

        transfer.setStatus(TicketTransferStatus.ACCEPTED);
        transfer.setRespondedAt(LocalDateTime.now());
        return toResponse(transferRepository.save(transfer));
    }

    /** Người nhận từ chối. */
    @Transactional
    public TicketTransferResponse decline(int transferId, String me) {
        TicketTransfer transfer = getPendingForRecipient(transferId, me);
        transfer.setStatus(TicketTransferStatus.DECLINED);
        transfer.setRespondedAt(LocalDateTime.now());
        return toResponse(transferRepository.save(transfer));
    }

    /** Người gửi thu hồi lời mời. */
    @Transactional
    public TicketTransferResponse cancel(int transferId, String me) {
        TicketTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_TRANSFER_NOT_FOUND));
        if (transfer.getStatus() != TicketTransferStatus.PENDING
                || !transfer.getFromUser().getID().equals(me))
            throw new AppException(ErrorCode.TICKET_TRANSFER_NOT_AUTHORIZED);
        transfer.setStatus(TicketTransferStatus.CANCELLED);
        transfer.setRespondedAt(LocalDateTime.now());
        return toResponse(transferRepository.save(transfer));
    }

    public List<TicketTransferResponse> listIncoming(String me) {
        return transferRepository.findByToUser_IDAndStatus(me, TicketTransferStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    public List<TicketTransferResponse> listOutgoing(String me) {
        return transferRepository.findByFromUser_IDAndStatus(me, TicketTransferStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    private TicketTransfer getPendingForRecipient(int transferId, String me) {
        TicketTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_TRANSFER_NOT_FOUND));
        if (transfer.getStatus() != TicketTransferStatus.PENDING
                || !transfer.getToUser().getID().equals(me))
            throw new AppException(ErrorCode.TICKET_TRANSFER_NOT_AUTHORIZED);
        return transfer;
    }

    /** Dựng response cho một transfer theo id (dùng để nhúng vào tin nhắn chat). */
    public TicketTransferResponse getResponse(int transferId) {
        return transferRepository.findById(transferId)
                .map(this::toResponse)
                .orElse(null);
    }

    private TicketTransferResponse toResponse(TicketTransfer t) {
        Booking b = t.getBooking();
        var schedule = b.getSchedule();

        String movieName = schedule != null && schedule.getMovie() != null
                ? schedule.getMovie().getMovieName() : null;
        String cinemaName = schedule != null && schedule.getRoom() != null
                && schedule.getRoom().getCinema() != null
                ? schedule.getRoom().getCinema().getCinemaName() : null;

        List<String> seatLabels = bookingSeatRepository.findAllByBooking_BookingId(b.getBookingId())
                .orElse(List.of()).stream()
                .map(bs -> {
                    Seat seat = bs.getSeatSchedule() != null ? bs.getSeatSchedule().getSeat() : null;
                    return seat != null ? "" + seat.getSeatRow() + seat.getSeatNumber() : "?";
                })
                .toList();

        return TicketTransferResponse.builder()
                .id(t.getId())
                .bookingId(b.getBookingId())
                .fromUserId(t.getFromUser().getID())
                .fromUsername(t.getFromUser().getUsername())
                .toUserId(t.getToUser().getID())
                .toUsername(t.getToUser().getUsername())
                .movieName(movieName)
                .cinemaName(cinemaName)
                .scheduleStart(schedule != null ? schedule.getScheduleStart() : null)
                .seats(seatLabels)
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .build();
    }

    public String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
