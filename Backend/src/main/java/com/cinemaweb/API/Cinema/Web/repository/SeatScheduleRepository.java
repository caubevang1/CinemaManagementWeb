package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.SeatSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatScheduleRepository extends JpaRepository<SeatSchedule, String> {
    SeatSchedule findBySeatScheduleId(int seatScheduleId);
}
