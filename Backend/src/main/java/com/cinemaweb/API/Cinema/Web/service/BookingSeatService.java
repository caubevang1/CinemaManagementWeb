package com.cinemaweb.API.Cinema.Web.service;


import com.cinemaweb.API.Cinema.Web.dto.request.BookingSeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingSeatResponse;
import com.cinemaweb.API.Cinema.Web.entity.BookingSeat;
import com.cinemaweb.API.Cinema.Web.entity.Seat;
import com.cinemaweb.API.Cinema.Web.entity.SeatSchedule;
import com.cinemaweb.API.Cinema.Web.mapper.BookingSeatMapper;
import com.cinemaweb.API.Cinema.Web.repository.BookingSeatRepository;
import com.cinemaweb.API.Cinema.Web.repository.SeatRepository;
import com.cinemaweb.API.Cinema.Web.repository.SeatScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingSeatService {
    BookingSeatRepository bookingSeatRepository;
    BookingSeatMapper bookingSeatMapper;
    SeatScheduleRepository seatScheduleRepository;

    public BookingSeatResponse get(int id) {
        return bookingSeatMapper.toBookingSeatResponse(bookingSeatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invalid booking seat!")));
    }


    public List<BookingSeatResponse> getAll() {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findAll();
        return bookingSeats.stream().map(bookingSeatMapper::toBookingSeatResponse).toList();
    }

    public BookingSeatResponse create(BookingSeatRequest request) {
        BookingSeat bookingSeat = bookingSeatMapper.toBookingSeat(request);
        SeatSchedule seatSchedule = seatScheduleRepository.findById(Integer.toString(request.getSeatScheduleId()))
                .orElseThrow(() -> new RuntimeException("Invalid seat!"));
        bookingSeat.setPrice(seatSchedule.getSeat().getSeatPrice());
        return  bookingSeatMapper.toBookingSeatResponse(bookingSeatRepository.save(bookingSeat));
    }
}
