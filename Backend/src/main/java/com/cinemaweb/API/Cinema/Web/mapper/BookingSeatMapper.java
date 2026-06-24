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
    @Mapping(target = "seatLabel", expression = "java(seatLabel(bookingSeat))")
    BookingSeatResponse toBookingSeatResponse(BookingSeat bookingSeat);

    // Nhãn ghế thật từ Seat: seatRow (char) + seatNumber (int) -> "A1".
    default String seatLabel(BookingSeat bs) {
        if (bs.getSeatSchedule() == null || bs.getSeatSchedule().getSeat() == null) {
            return null;
        }
        var s = bs.getSeatSchedule().getSeat();
        return s.getSeatRow() + String.valueOf(s.getSeatNumber());
    }


    @Mapping(source = "seatScheduleId", target = "seatSchedule.seatScheduleId")
    BookingSeat toBookingSeat(BookingSeatRequest request);
}
