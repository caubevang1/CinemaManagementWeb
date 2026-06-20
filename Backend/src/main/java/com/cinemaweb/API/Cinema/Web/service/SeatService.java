package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.SeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatResponse;
import com.cinemaweb.API.Cinema.Web.entity.Room;
import com.cinemaweb.API.Cinema.Web.entity.Seat;
import com.cinemaweb.API.Cinema.Web.mapper.SeatMapper;
import com.cinemaweb.API.Cinema.Web.repository.RoomRepository;
import com.cinemaweb.API.Cinema.Web.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RoomRepository roomRepository;

    public List<SeatResponse> getAllSeats() {
        return seatMapper.toSeatResponseList(seatRepository.findAll());
    }

    public SeatResponse getSeat(String seatId) {
        return seatMapper.toSeatResponse(seatRepository.findById(seatId).orElseThrow(()
                -> new RuntimeException("Seat id is not found")));
    }

    public void createSeat(SeatRequest seatCreateRequest) {
        Seat seat = seatMapper.toSeatCreate(seatCreateRequest);
        seatRepository.save(seat);
    }

    public void updateSeat(String seatId, SeatRequest seatUpdateRequest) {
        Room room = roomRepository.findById(Integer.toString(seatUpdateRequest.getRoomId()))
                .orElseThrow(() -> new RuntimeException("Room id in updateSeat is not found!"));

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat id in updateSeat is not found!"));

        seat.setRoom(room);
        seatMapper.toSeatUpdate(seat,seatUpdateRequest);
        seatRepository.save(seat);
    }

    public void deleteSeat(String seatId) {
        seatRepository.deleteById(seatId);
    }
}
