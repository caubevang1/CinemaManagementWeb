package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingSeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingSeatResponse;
import com.cinemaweb.API.Cinema.Web.entity.BookingSeat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingSeatMapper {
    @Mapping(source = "booking.bookingId", target = "bookingId")
    @Mapping(source = "seatSchedule.seatScheduleId", target = "seatScheduleId")
    BookingSeatResponse toBookingSeatResponse(BookingSeat bookingSeat);


    @Mapping(source = "seatScheduleId", target = "seatSchedule.seatScheduleId")
    BookingSeat toBookingSeat(BookingSeatRequest request);
}
