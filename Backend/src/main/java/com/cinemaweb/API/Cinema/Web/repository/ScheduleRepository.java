package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.Schedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule,String> {

    // Nạp kèm movie/room/room.cinema trong cùng truy vấn (đều LAZY) để mapper dựng
    // response không sinh N+1 và không phụ thuộc Open-Session-In-View.
    @Override
    @EntityGraph(attributePaths = {"movie", "room", "room.cinema"})
    List<Schedule> findAll();

    @Override
    @EntityGraph(attributePaths = {"movie", "room", "room.cinema"})
    Optional<Schedule> findById(String id);

    // Có tồn tại suất chiếu khác trong cùng phòng bị chồng khung giờ?
    // Hai khoảng [start,end) chồng nhau khi: existing.start < new.end AND existing.end > new.start.
    // Dùng DATETIME nên xử lý đúng cả suất chiếu qua nửa đêm.
    @Query("select count(s) > 0 from Schedule s " +
            "where s.room.roomId = :roomId " +
            "and s.scheduleStart < :end and s.scheduleEnd > :start")
    boolean existsOverlappingSchedule(@Param("roomId") int roomId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    // Bản loại trừ chính suất đang cập nhật (tránh tự coi mình là trùng).
    @Query("select count(s) > 0 from Schedule s " +
            "where s.room.roomId = :roomId and s.scheduleId <> :excludeId " +
            "and s.scheduleStart < :end and s.scheduleEnd > :start")
    boolean existsOverlappingScheduleExcluding(@Param("roomId") int roomId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("excludeId") int excludeId);
}
