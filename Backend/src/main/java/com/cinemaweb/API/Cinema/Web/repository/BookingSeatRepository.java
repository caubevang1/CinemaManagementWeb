package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {
    // Kiểu Optional sẽ có hai trường hợp:
    // + Trả về kiểu truyền vào
    // + Trả về empty nếu ko tìm thấy dữ liệu trùng khớp
    //      khi gọi không cần check exsist
    //      --> chỉ cần BookingSeatRepository.find____().orElseThrow(()-> exception)
    public Optional<List<BookingSeat>> findAllByBooking_BookingId(int bookingId);

    // Batch load cho nhiều booking (tránh N+1 trong getAllMyBooking)
    List<BookingSeat> findAllByBooking_BookingIdIn(List<Integer> bookingIds);

    // Các ghế-suất của một suất chiếu đang gắn booking_seat (hard hold pha thanh toán).
    // booking CANCELLED đã xoá booking_seat, booking PAID đã set seat_state=BOOKED, nên
    // các ghế còn ở đây mà chưa BOOKED tức là đơn PENDING đang chờ thanh toán -> coi như HELD.
    @Query("select bs.seatSchedule.seatScheduleId from BookingSeat bs " +
            "where bs.seatSchedule.schedule.scheduleId = :scheduleId")
    List<Integer> findHeldSeatScheduleIdsBySchedule(@Param("scheduleId") int scheduleId);
}
