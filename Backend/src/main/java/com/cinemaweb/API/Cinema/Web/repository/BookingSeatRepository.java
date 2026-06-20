package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
