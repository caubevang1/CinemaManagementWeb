package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.SeatSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatScheduleRepository extends JpaRepository<SeatSchedule, String> {
    Optional<SeatSchedule> findBySeatScheduleId(int seatScheduleId);

    // Lọc ghế theo suất chiếu ngay tại DB (thay cho findAll() rồi lọc ở client).
    @EntityGraph(attributePaths = "seat")
    List<SeatSchedule> findBySchedule_ScheduleId(int scheduleId);

    // Khóa bi quan (SELECT ... FOR UPDATE) các ghế-suất khi giữ/đặt để chống race:
    // hai request cùng chọn 1 ghế sẽ tuần tự hóa, người sau thấy trạng thái đã đổi.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from SeatSchedule ss where ss.seatScheduleId in :ids")
    List<SeatSchedule> findForUpdate(@Param("ids") List<Integer> ids);
}
