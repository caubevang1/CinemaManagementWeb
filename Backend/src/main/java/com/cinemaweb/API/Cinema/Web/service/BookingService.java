package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingFoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.BookingRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.BookingSeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingFoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatResponse;
import com.cinemaweb.API.Cinema.Web.entity.*;
import com.cinemaweb.API.Cinema.Web.enums.SeatState;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.BookingFoodAndDrinkMapper;
import com.cinemaweb.API.Cinema.Web.mapper.BookingMapper;
import com.cinemaweb.API.Cinema.Web.mapper.BookingSeatMapper;
import com.cinemaweb.API.Cinema.Web.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private BookingFoodAndDrinkRepository bookingFoodAndDrinkRepository;

    @Autowired
    private BookingFoodAndDrinkMapper bookingFoodAndDrinkMapper;

    @Autowired
    private BookingSeatMapper bookingSeatMapper;

    @Autowired
    private SeatScheduleRepository seatScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodAndDrinkRepository foodAndDrinkRepository;

    public BookingResponse getBooking(String bookingId) {
        int bookingIdInt = Integer.parseInt(bookingId);
        List<BookingFoodAndDrinkResponse> listBookingFoodAndDrinks = new ArrayList<>();
        if (bookingFoodAndDrinkRepository.existsByBooking_BookingId(bookingIdInt)) {
            listBookingFoodAndDrinks = bookingFoodAndDrinkMapper.toListBookingFoodAndDrinks(
                    bookingFoodAndDrinkRepository.findByBooking_BookingId(bookingIdInt));
        }
        BookingResponse booking = bookingMapper.toBookingResponse(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking id is not found")));
        // Lấy về các seat của booking
        List<BookingSeat> seats = bookingSeatRepository.findAllByBooking_BookingId(bookingIdInt)
                .orElseThrow(() -> new RuntimeException("Invalid seats"));
        booking.setFoodAndDrinks(listBookingFoodAndDrinks);
        booking.setSeats(seats.stream().map(bookingSeatMapper::toBookingSeatResponse).toList());
        return booking;
    }



    public List<BookingResponse> getAllMyBooking() {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        var bookings =  bookingRepository.findAllByUser_ID(userId)
                .orElseThrow(() -> new RuntimeException("User chua tung co hoa don nao!"));

        List<Integer> bookingIds = bookings.stream().map(Booking::getBookingId).toList();
        if (bookingIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Batch load: 1 query cho seats, 1 query cho food&drink (thay vì N+1 per booking)
        Map<Integer, List<BookingSeat>> seatsByBooking =
                bookingSeatRepository.findAllByBooking_BookingIdIn(bookingIds).stream()
                        .collect(Collectors.groupingBy(bs -> bs.getBooking().getBookingId()));
        Map<Integer, List<BookingFoodAndDrink>> foodsByBooking =
                bookingFoodAndDrinkRepository.findByBooking_BookingIdIn(bookingIds).stream()
                        .collect(Collectors.groupingBy(bf -> bf.getBooking().getBookingId()));

        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (Booking booking : bookings) {
            int bookingId = booking.getBookingId();
            BookingResponse response = bookingMapper.toBookingResponse(booking);
            response.setSeats(seatsByBooking.getOrDefault(bookingId, List.of()).stream()
                    .map(bookingSeatMapper::toBookingSeatResponse).toList());
            response.setFoodAndDrinks(bookingFoodAndDrinkMapper.toListBookingFoodAndDrinks(
                    foodsByBooking.getOrDefault(bookingId, List.of())));
            bookingResponses.add(response);
        }
        return bookingResponses;
    }

    @Transactional
    public void createBooking(BookingRequest bookingRequest) {
        Booking booking = bookingMapper.toCreationBooking(bookingRequest);
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        booking.setUser(user);
        bookingRepository.save(booking);
        // Tinh tien seat

        BigDecimal seatPrice = BigDecimal.ZERO;
        List<BookingSeatRequest> bookingSeats = bookingRequest.getSeats();
        List<Integer> seatScheduleIds = bookingSeats.stream()
                .map(BookingSeatRequest::getSeatScheduleId).toList();

        // Khóa bi quan (SELECT ... FOR UPDATE) các ghế-suất để tuần tự hóa hai
        // request cùng chọn 1 ghế -> chống race condition trước cả chốt chặn UNIQUE.
        List<SeatSchedule> seatSchedules = seatScheduleRepository.findForUpdate(seatScheduleIds);
        if (seatSchedules.size() != seatScheduleIds.size()) {
            throw new AppException(ErrorCode.SEAT_SCHEDULE_NOT_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        List<BookingSeat> bookingSeatEntities = new ArrayList<>();
        for (SeatSchedule seatSchedule : seatSchedules) {
            // Chỉ đặt được khi ghế đang trống, hoặc đang được CHÍNH user này giữ và chưa hết hạn.
            boolean heldByMe = seatSchedule.getSeatState() == SeatState.HELD
                    && seatSchedule.getHeldBy() != null
                    && id.equals(seatSchedule.getHeldBy().getID())
                    && seatSchedule.getHeldUntil() != null
                    && seatSchedule.getHeldUntil().isAfter(now);
            if (seatSchedule.getSeatState() == SeatState.BOOKED) {
                throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
            }
            if (seatSchedule.getSeatState() == SeatState.HELD && !heldByMe) {
                throw new AppException(ErrorCode.SEAT_HELD_BY_OTHER);
            }

            seatSchedule.setSeatState(SeatState.BOOKED);
            seatSchedule.setHeldUntil(null);
            seatSchedule.setHeldBy(null);
            seatPrice = seatPrice.add(seatSchedule.getPrice());

            bookingSeatEntities.add(BookingSeat.builder()
                    .booking(booking)
                    .seatSchedule(seatSchedule)
                    .price(seatSchedule.getPrice())
                    .build());
        }
        // UNIQUE(seat_schedule_id) ở DB là chốt chặn cuối; flush ngay để bắt lỗi
        // ngay tại đây và trả về lỗi nghiệp vụ rõ ràng thay vì lỗi 500.
        try {
            bookingSeatRepository.saveAll(bookingSeatEntities);
            bookingSeatRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
        }
        seatScheduleRepository.saveAll(seatSchedules);

        BigDecimal foodAndDrinksPrice = BigDecimal.ZERO;
        if(bookingRequest.getFoodAndDrinks() != null) {
            List<BookingFoodAndDrinkRequest> listBookingFoodAndDrink =
                    bookingRequest.getFoodAndDrinks();

            for (int i = 0; i < listBookingFoodAndDrink.size(); i++ ) {
                FoodAndDrink foodAndDrink = foodAndDrinkRepository
                        .findById(String.valueOf(listBookingFoodAndDrink.get(i).getFoodAndDrinkId()))
                        .orElseThrow(() -> new RuntimeException("F&D id is not found"));
                BigDecimal lineTotal = foodAndDrink.getFoodAndDrinkPrice()
                        .multiply(BigDecimal.valueOf(listBookingFoodAndDrink.get(i).getQuantity()));
                foodAndDrinksPrice = foodAndDrinksPrice.add(lineTotal);

                BookingFoodAndDrink bookingFoodAndDrink = BookingFoodAndDrink.builder()
                        .foodAndDrink(foodAndDrink)
                        .booking(booking)
                        .quantity(listBookingFoodAndDrink.get(i).getQuantity())
                        .price(lineTotal)
                        .build();

                bookingFoodAndDrinkRepository.save(bookingFoodAndDrink);
            }
        }

        booking.setPrice(seatPrice.add(foodAndDrinksPrice));
        bookingRepository.save(booking);
    }
}
