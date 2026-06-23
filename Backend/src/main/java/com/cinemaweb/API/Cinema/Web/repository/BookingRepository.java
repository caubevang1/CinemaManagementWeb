package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.entity.Booking;
import com.cinemaweb.API.Cinema.Web.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking,String> {
    public Optional<List<Booking>> findAllByUser_ID(String userID);

    // Các đơn còn treo (PENDING) đã quá hạn giữ ghế (expires_at) -> ứng viên để cron tự huỷ.
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime time);
}
