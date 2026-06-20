package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.RoomRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.RoomResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.entity.Room;
import com.cinemaweb.API.Cinema.Web.entity.Seat;
import com.cinemaweb.API.Cinema.Web.mapper.RoomMapper;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import com.cinemaweb.API.Cinema.Web.repository.RoomRepository;
import com.cinemaweb.API.Cinema.Web.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private SeatRepository seatRepository;

    public List<RoomResponse> getAllRooms() {
        return roomMapper.toRoomResponseList(roomRepository.findAll());
    }

    public RoomResponse getRoom(String id) {
        return roomMapper.toRoomResponse(roomRepository.findById(id).orElseThrow(()
                -> new RuntimeException("Room id not found")));
    }

    public void createRoom(RoomRequest roomCreateRequest) {
        List<Seat> seats = new ArrayList<>();
        int numRows = roomCreateRequest.getNumRow();
        int numCols = roomCreateRequest.getNumCol();
        double[] seatPrice = roomCreateRequest.getSeatPrice();
        Room room = roomMapper.toCreateRoom(roomCreateRequest);

        roomRepository.save(room);

        for (int row = 1; row <= numRows; row++) {
            char alphabetRow = (char) (row + 64);
            for (int col = 1; col <= numCols; col++) {
                Seat seat = Seat.builder()
                        .room(room)
                        .seatType("")
                        .seatRow(alphabetRow)
                        .seatNumber(col)
                        .seatPrice(100000)
                        .build();
                if(row == numRows) {
                    seat.setSeatType("Couple");
                    seat.setSeatPrice(seatPrice[0]);
                } else if(row <= 3) {
                    seat.setSeatType("Normal");
                    seat.setSeatPrice(seatPrice[1]);
                } else {
                    seat.setSeatType("VIP");
                    seat.setSeatPrice(seatPrice[2]);
                }
                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
    }

    public void updateRoom(String roomId,RoomRequest roomUpdateRequest) {
        Room room = roomRepository.findById(roomId).orElseThrow(()
                -> new RuntimeException("Room id is not found!"));

        //Create new cinema and set cinemaId in room
        Cinema cinema = cinemaRepository.findById(Integer.toString(roomUpdateRequest.getCinemaId()))
                .orElseThrow(() -> new RuntimeException("Cinema from room is not found!"));
        cinema.setCinemaId(roomUpdateRequest.getCinemaId());
        room.setCinema(cinema);

        roomMapper.toUpdateRoom(room, roomUpdateRequest);
        roomRepository.save(room);
    }

    public void deleteRoom(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(()
                -> new RuntimeException("Room id is not found"));
        roomRepository.delete(room);
    }
}
