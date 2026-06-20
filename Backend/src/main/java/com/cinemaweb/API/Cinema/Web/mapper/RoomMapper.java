package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.RoomRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.RoomResponse;
import com.cinemaweb.API.Cinema.Web.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(source= "cinema.cinemaId", target="cinemaId")
    @Mapping(source = "cinema.cinemaName", target = "cinemaName")
    RoomResponse toRoomResponse(Room room);

    @Mapping(source = "cinemaId", target = "cinema.cinemaId")
    Room toCreateRoom(RoomRequest roomCreateRequest);

    @Mapping(target = "cinema", ignore = true)
    void toUpdateRoom(@MappingTarget Room room, RoomRequest RoomUpdateRequest);

    @Mapping(source= "cinema.cinemaId", target="cinemaId")
    @Mapping(source = "cinema.cinemaName", target = "cinemaName")
    List<RoomResponse> toRoomResponseList(List<Room> room);
}
