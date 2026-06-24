package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.response.SeatScheduleResponse;
import com.cinemaweb.API.Cinema.Web.entity.SeatSchedule;
import com.cinemaweb.API.Cinema.Web.enums.SeatState;
import com.cinemaweb.API.Cinema.Web.event.SeatsChangedEvent;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.SeatScheduleMapper;
import com.cinemaweb.API.Cinema.Web.repository.BookingSeatRepository;
import com.cinemaweb.API.Cinema.Web.repository.SeatScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class SeatScheduleService {
    @Autowired
    SeatScheduleRepository seatScheduleRepository;

    @Autowired
    SeatScheduleMapper seatScheduleMapper;

    @Autowired
    BookingSeatRepository bookingSeatRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    // Thời hạn giữ ghế tạm (phút) khi user đang thanh toán.
    @Value("${booking.hold-minutes:8}")
    long holdMinutes;

    // Redis key giữ ghế tạm: seat_hold:{seatScheduleId} -> userId (TTL = holdMinutes, tự hết hạn).
    static final String SEAT_HOLD_PREFIX = "seat_hold:";

    static String holdKey(int seatScheduleId) {
        return SEAT_HOLD_PREFIX + seatScheduleId;
    }

    public List<SeatScheduleResponse> getListSeatSchedule() {
        return seatScheduleMapper.toListSeatSchedule(seatScheduleRepository.findAll());
    }

    // Lọc ghế theo suất chiếu ngay tại DB, rồi overlay trạng thái HELD từ Redis (giữ tạm) và
    // từ booking_seat PENDING (đang thanh toán) — vì DB chỉ còn lưu AVAILABLE/BOOKED.
    public List<SeatScheduleResponse> getListSeatScheduleBySchedule(int scheduleId) {
        String userId = currentUserIdOrNull();
        List<SeatScheduleResponse> responses = seatScheduleMapper.toListSeatSchedule(
                seatScheduleRepository.findBySchedule_ScheduleId(scheduleId));

        // 1 lệnh multiGet thay vì N lệnh GET tới Redis.
        List<String> keys = responses.stream().map(r -> holdKey(r.getSeatScheduleId())).toList();
        List<String> holders = keys.isEmpty() ? List.of() : redisTemplate.opsForValue().multiGet(keys);
        Set<Integer> pendingHeld = new HashSet<>(
                bookingSeatRepository.findHeldSeatScheduleIdsBySchedule(scheduleId));

        for (int i = 0; i < responses.size(); i++) {
            SeatScheduleResponse r = responses.get(i);
            if ("BOOKED".equals(r.getSeatState())) continue;

            String holder = holders == null ? null : holders.get(i);
            if (holder != null) {
                applyHeld(r, holder, userId);
            } else if (pendingHeld.contains(r.getSeatScheduleId())) {
                // Đang chờ thanh toán (hard hold ở DB) -> coi như HELD bởi người khác.
                r.setSeatState("HELD");
            }
        }
        return responses;
    }

    // Giữ ghế tạm trong Redis: mỗi ghế setIfAbsent (SETNX có TTL) -> chống race cho hai
    // request cùng chọn 1 ghế. Ghế đã đặt (DB BOOKED) hoặc đang bị người khác giữ -> báo lỗi.
    // Ghế đang do CHÍNH user này giữ thì OK và GIỮ NGUYÊN TTL cũ (không reset đồng hồ).
    @Transactional
    public List<SeatScheduleResponse> holdSeats(List<Integer> seatScheduleIds) {
        String userId = currentUserId();

        List<SeatSchedule> seatSchedules = seatScheduleRepository.findForUpdate(seatScheduleIds);
        if (seatSchedules.size() != seatScheduleIds.size()) {
            throw new AppException(ErrorCode.SEAT_SCHEDULE_NOT_EXISTS);
        }

        List<Integer> acquired = new ArrayList<>(); // các key vừa set trong lô này -> rollback nếu fail
        try {
            for (SeatSchedule ss : seatSchedules) {
                if (ss.getSeatState() == SeatState.BOOKED) {
                    throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
                }
                String key = holdKey(ss.getSeatScheduleId());
                Boolean ok = redisTemplate.opsForValue()
                        .setIfAbsent(key, userId, holdMinutes, TimeUnit.MINUTES);
                if (Boolean.TRUE.equals(ok)) {
                    acquired.add(ss.getSeatScheduleId());
                } else {
                    String holder = redisTemplate.opsForValue().get(key);
                    if (!userId.equals(holder)) {
                        throw new AppException(ErrorCode.SEAT_HELD_BY_OTHER);
                    }
                    // đã do chính mình giữ -> không set lại để giữ nguyên TTL (deadline duy nhất)
                }
            }
        } catch (RuntimeException ex) {
            acquired.forEach(id -> redisTemplate.delete(holdKey(id)));
            throw ex;
        }

        // Báo realtime cho các client đang xem suất này (ghế chuyển sang HELD).
        publishSeatsChanged(seatSchedules.get(0).getSchedule().getScheduleId());

        return seatSchedules.stream().map(ss -> {
            SeatScheduleResponse r = seatScheduleMapper.toSeatSchedule(ss);
            applyHeld(r, userId, userId);
            return r;
        }).toList();
    }

    // Nhả ghế đang được CHÍNH user này giữ (ví dụ quay lại chọn ghế). Bỏ qua ghế của người khác.
    @Transactional
    public void releaseSeats(List<Integer> seatScheduleIds) {
        String userId = currentUserId();
        for (Integer id : seatScheduleIds) {
            String key = holdKey(id);
            if (userId.equals(redisTemplate.opsForValue().get(key))) {
                redisTemplate.delete(key);
            }
        }
        // Báo realtime: ghế trở lại AVAILABLE. Lấy scheduleId từ 1 ghế bất kỳ trong lô.
        if (!seatScheduleIds.isEmpty()) {
            seatScheduleRepository.findBySeatScheduleId(seatScheduleIds.get(0))
                    .ifPresent(ss -> publishSeatsChanged(ss.getSchedule().getScheduleId()));
        }
    }

    // Phát domain event; SeatsChangedBridge broadcast STOMP sau khi transaction commit.
    public void publishSeatsChanged(int scheduleId) {
        eventPublisher.publishEvent(new SeatsChangedEvent(scheduleId));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    // Gán trạng thái HELD + heldUntil (từ TTL Redis còn lại) + heldByMe lên response.
    private void applyHeld(SeatScheduleResponse r, String holder, String currentUserId) {
        r.setSeatState("HELD");
        r.setHeldByMe(currentUserId != null && currentUserId.equals(holder));
        Long ttl = redisTemplate.getExpire(holdKey(r.getSeatScheduleId()), TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            r.setHeldUntil(LocalDateTime.now().plusSeconds(ttl));
        }
    }

    private String currentUserId() {
        String id = currentUserIdOrNull();
        if (id == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
        return id;
    }

    private String currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null || auth.getName() == null) ? null : auth.getName();
    }
}
