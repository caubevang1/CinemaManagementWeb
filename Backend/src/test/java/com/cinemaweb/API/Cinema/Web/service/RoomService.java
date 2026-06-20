package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.RoomRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.RoomResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.entity.Room;
import com.cinemaweb.API.Cinema.Web.mapper.RoomMapper;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import com.cinemaweb.API.Cinema.Web.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CinemaRepository cinemaRepository;

    public List<RoomResponse> getAllRooms() {
        return roomMapper.toRoomResponseList(roomRepository.findAll());
    }

    public RoomResponse getRoom(String id) {
        return roomMapper.toRoomResponse(roomRepository.findById(id).orElseThrow(()
                -> new RuntimeException("Room id not found")));
    }

    public void createRoom(RoomRequest roomCreateRequest) {
        roomRepository.save(roomMapper.toCreateRoom(roomCreateRequest));
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
