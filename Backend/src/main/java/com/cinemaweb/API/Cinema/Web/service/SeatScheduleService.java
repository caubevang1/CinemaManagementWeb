package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.response.SeatScheduleResponse;
import com.cinemaweb.API.Cinema.Web.entity.SeatSchedule;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.enums.SeatState;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.SeatScheduleMapper;
import com.cinemaweb.API.Cinema.Web.repository.SeatScheduleRepository;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatScheduleService {
    @Autowired
    SeatScheduleRepository seatScheduleRepository;

    @Autowired
    SeatScheduleMapper seatScheduleMapper;

    @Autowired
    UserRepository userRepository;

    // Thời hạn giữ ghế tạm (phút) khi user đang thanh toán.
    @Value("${booking.hold-minutes:8}")
    long holdMinutes;

    public List<SeatScheduleResponse> getListSeatSchedule() {
        return seatScheduleMapper.toListSeatSchedule(seatScheduleRepository.findAll());
    }

    // Lọc ghế theo suất chiếu ngay tại DB thay vì trả toàn bảng rồi lọc ở client.
    public List<SeatScheduleResponse> getListSeatScheduleBySchedule(int scheduleId) {
        return seatScheduleMapper.toListSeatSchedule(
                seatScheduleRepository.findBySchedule_ScheduleId(scheduleId));
    }

    // Giữ ghế tạm cho user đang thanh toán: khóa bi quan rồi chuyển AVAILABLE -> HELD
    // kèm held_until. Ghế đã đặt hoặc đang bị người khác giữ (chưa hết hạn) -> báo lỗi.
    @Transactional
    public List<SeatScheduleResponse> holdSeats(List<Integer> seatScheduleIds) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        List<SeatSchedule> seatSchedules = seatScheduleRepository.findForUpdate(seatScheduleIds);
        if (seatSchedules.size() != seatScheduleIds.size()) {
            throw new AppException(ErrorCode.SEAT_SCHEDULE_NOT_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        for (SeatSchedule ss : seatSchedules) {
            if (ss.getSeatState() == SeatState.BOOKED) {
                throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
            }
            boolean heldByOther = ss.getSeatState() == SeatState.HELD
                    && ss.getHeldBy() != null
                    && !userId.equals(ss.getHeldBy().getID())
                    && ss.getHeldUntil() != null
                    && ss.getHeldUntil().isAfter(now);
            if (heldByOther) {
                throw new AppException(ErrorCode.SEAT_HELD_BY_OTHER);
            }

            ss.setSeatState(SeatState.HELD);
            ss.setHeldBy(user);
            ss.setHeldUntil(now.plusMinutes(holdMinutes));
        }
        seatScheduleRepository.saveAll(seatSchedules);
        return seatScheduleMapper.toListSeatSchedule(seatSchedules);
    }

    // Nhả ghế đang được CHÍNH user này giữ (ví dụ huỷ thanh toán). Bỏ qua ghế đã đặt.
    @Transactional
    public void releaseSeats(List<Integer> seatScheduleIds) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<SeatSchedule> seatSchedules = seatScheduleRepository.findForUpdate(seatScheduleIds);
        for (SeatSchedule ss : seatSchedules) {
            if (ss.getSeatState() == SeatState.HELD
                    && ss.getHeldBy() != null
                    && userId.equals(ss.getHeldBy().getID())) {
                ss.setSeatState(SeatState.AVAILABLE);
                ss.setHeldBy(null);
                ss.setHeldUntil(null);
            }
        }
        seatScheduleRepository.saveAll(seatSchedules);
    }
}
