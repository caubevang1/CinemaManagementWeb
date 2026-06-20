package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(source = "schedule.movie.movieName",target = "movieName")
    @Mapping(source = "schedule.room.roomName",target = "roomName")
    @Mapping(source = "schedule.cinema.cinemaName",target = "cinemaName")
    @Mapping(source = "user.username",target = "username")
    BookingResponse toBookingResponse(Booking booking);

    @Mapping(source = "scheduleId", target = "schedule.scheduleId")
    Booking toCreationBooking(BookingRequest bookingRequest);
}
