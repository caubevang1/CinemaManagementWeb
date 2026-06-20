package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.SeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatResponse;
import com.cinemaweb.API.Cinema.Web.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    @Mapping(source = "room.roomName", target = "roomName")
    @Mapping(source = "room.roomId", target = "roomId")
    SeatResponse toSeatResponse(Seat seat);

    @Mapping(source = "room.roomName", target = "roomName")
    @Mapping(source = "room.roomId", target = "roomId")
    List<SeatResponse> toSeatResponseList(List<Seat> seat);

    @Mapping(source = "roomId", target = "room.roomId")
    Seat toSeatCreate(SeatRequest seatCreateRequest);

    @Mapping(target = "room", ignore = true)
    void toSeatUpdate(@MappingTarget Seat seat, SeatRequest seatUpdateRequest);
}
