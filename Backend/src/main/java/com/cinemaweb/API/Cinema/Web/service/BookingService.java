package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.BookingFoodAndDrinkRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.BookingRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.BookingSeatRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingFoodAndDrinkResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.SeatResponse;
import com.cinemaweb.API.Cinema.Web.configuration.ConfigPayment;
import com.cinemaweb.API.Cinema.Web.entity.*;
import com.cinemaweb.API.Cinema.Web.enums.BookingStatus;
import com.cinemaweb.API.Cinema.Web.enums.PaymentStatus;
import com.cinemaweb.API.Cinema.Web.enums.SeatState;
import com.cinemaweb.API.Cinema.Web.enums.TicketTransferStatus;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.BookingFoodAndDrinkMapper;
import com.cinemaweb.API.Cinema.Web.mapper.BookingMapper;
import com.cinemaweb.API.Cinema.Web.mapper.BookingSeatMapper;
import com.cinemaweb.API.Cinema.Web.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private TicketTransferRepository ticketTransferRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Thời hạn giữ ghế tạm (phút) khi user đang thanh toán — đồng bộ với SeatScheduleService.
    @Value("${booking.hold-minutes:8}")
    private long holdMinutes;

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
        List<Booking> owned = bookingRepository.findAllByUser_ID(userId).orElse(List.of());

        // Vé đã chuyển nhượng đi (ACCEPTED): vẫn giữ bản ghi cho người gửi, gắn nhãn người nhận.
        Map<Integer, String> transferredTo = new HashMap<>();
        List<Booking> transferred = new ArrayList<>();
        for (TicketTransfer t : ticketTransferRepository.findByFromUser_IDAndStatus(
                userId, TicketTransferStatus.ACCEPTED)) {
            Booking b = t.getBooking();
            if (b == null) continue;
            transferredTo.put(b.getBookingId(), t.getToUser().getUsername());
            transferred.add(b);
        }

        List<Booking> all = new ArrayList<>(owned);
        all.addAll(transferred);
        if (all.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> bookingIds = all.stream().map(Booking::getBookingId).toList();

        // Batch load: 1 query cho seats, 1 query cho food&drink (thay vì N+1 per booking)
        Map<Integer, List<BookingSeat>> seatsByBooking =
                bookingSeatRepository.findAllByBooking_BookingIdIn(bookingIds).stream()
                        .collect(Collectors.groupingBy(bs -> bs.getBooking().getBookingId()));
        Map<Integer, List<BookingFoodAndDrink>> foodsByBooking =
                bookingFoodAndDrinkRepository.findByBooking_BookingIdIn(bookingIds).stream()
                        .collect(Collectors.groupingBy(bf -> bf.getBooking().getBookingId()));

        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (Booking booking : all) {
            int bookingId = booking.getBookingId();
            BookingResponse response = bookingMapper.toBookingResponse(booking);
            response.setSeats(seatsByBooking.getOrDefault(bookingId, List.of()).stream()
                    .map(bookingSeatMapper::toBookingSeatResponse).toList());
            response.setFoodAndDrinks(bookingFoodAndDrinkMapper.toListBookingFoodAndDrinks(
                    foodsByBooking.getOrDefault(bookingId, List.of())));
            response.setTransferredToUsername(transferredTo.get(bookingId));
            bookingResponses.add(response);
        }
        return bookingResponses;
    }

    // Tạo đơn đặt vé ở trạng thái PENDING: giữ ghế HELD (chưa BOOKED) + tạo bản ghi payment
    // PENDING. Ghế chỉ chuyển BOOKED khi thanh toán thành công (confirmBookingPaid). Trả về
    // Payment để controller dựng URL VNPay redirect người dùng sang thanh toán.
    @Transactional
    public Payment createBooking(BookingRequest bookingRequest) {
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        LocalDateTime now = LocalDateTime.now();

        // ── 1) Tính tiền ghế + re-validate (check lại lần nữa qua Redis) trước khi tạo đơn ──
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

        // Deadline đơn = mốc hết hạn giữ ghế sớm nhất còn lại trong Redis (giữ nguyên đồng hồ,
        // không reset). Nếu ghế chưa được pre-hold thì mặc định now + holdMinutes.
        long remainingTtlSeconds = holdMinutes * 60;
        for (SeatSchedule seatSchedule : seatSchedules) {
            if (seatSchedule.getSeatState() == SeatState.BOOKED) {
                throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
            }
            String key = SeatScheduleService.holdKey(seatSchedule.getSeatScheduleId());
            String holder = redisTemplate.opsForValue().get(key);
            if (holder != null && !id.equals(holder)) {
                throw new AppException(ErrorCode.SEAT_HELD_BY_OTHER);
            }
            if (id.equals(holder)) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    remainingTtlSeconds = Math.min(remainingTtlSeconds, ttl);
                }
            }
            seatPrice = seatPrice.add(seatSchedule.getPrice());
        }
        LocalDateTime expiresAt = now.plusSeconds(remainingTtlSeconds);

        // ── 2) Tính tiền đồ ăn (dựng sẵn entity, chưa gắn booking) ──
        BigDecimal foodAndDrinksPrice = BigDecimal.ZERO;
        List<BookingFoodAndDrink> bookingFoodAndDrinkEntities = new ArrayList<>();
        if (bookingRequest.getFoodAndDrinks() != null) {
            for (BookingFoodAndDrinkRequest fdRequest : bookingRequest.getFoodAndDrinks()) {
                FoodAndDrink foodAndDrink = foodAndDrinkRepository
                        .findById(String.valueOf(fdRequest.getFoodAndDrinkId()))
                        .orElseThrow(() -> new RuntimeException("F&D id is not found"));
                BigDecimal lineTotal = foodAndDrink.getFoodAndDrinkPrice()
                        .multiply(BigDecimal.valueOf(fdRequest.getQuantity()));
                foodAndDrinksPrice = foodAndDrinksPrice.add(lineTotal);

                bookingFoodAndDrinkEntities.add(BookingFoodAndDrink.builder()
                        .foodAndDrink(foodAndDrink)
                        .quantity(fdRequest.getQuantity())
                        .price(lineTotal)
                        .build());
            }
        }

        // ── 3) Tạo Booking PENDING với price chuẩn ngay từ đầu, lưu đúng 1 lần ──
        BigDecimal totalPrice = seatPrice.add(foodAndDrinksPrice);
        Booking booking = bookingMapper.toCreationBooking(bookingRequest);
        booking.setUser(user);
        booking.setPrice(totalPrice);
        booking.setBookingDay(now);
        booking.setExpiresAt(expiresAt);
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        // ── 4) Gắn booking vào các bảng con rồi lưu ──
        List<BookingSeat> bookingSeatEntities = new ArrayList<>();
        for (SeatSchedule seatSchedule : seatSchedules) {
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
        // Đã có hard hold ở DB (booking_seat + UNIQUE) -> nhả soft hold Redis để khỏi đếm trùng.
        for (SeatSchedule seatSchedule : seatSchedules) {
            redisTemplate.delete(SeatScheduleService.holdKey(seatSchedule.getSeatScheduleId()));
        }

        for (BookingFoodAndDrink bookingFoodAndDrink : bookingFoodAndDrinkEntities) {
            bookingFoodAndDrink.setBooking(booking);
            bookingFoodAndDrinkRepository.save(bookingFoodAndDrink);
        }

        // ── 5) Tạo bản ghi payment PENDING (txn_ref dùng để tra ngược khi VNPay callback) ──
        Payment payment = Payment.builder()
                .booking(booking)
                .txnRef(ConfigPayment.getRandomNumber(8))
                .amount(totalPrice)
                .status(PaymentStatus.PENDING)
                .method("VNPAY")
                .createdAt(now)
                .build();
        return paymentRepository.save(payment);
    }

    // Thanh toán thành công: đơn -> PAID, ghế (đang hard hold qua booking_seat) chuyển BOOKED.
    @Transactional
    public void confirmBookingPaid(Booking booking) {
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
        for (BookingSeat bs : bookingSeatRepository.findAllByBooking_BookingId(booking.getBookingId())
                .orElse(List.of())) {
            SeatSchedule ss = bs.getSeatSchedule();
            ss.setSeatState(SeatState.BOOKED);
            seatScheduleRepository.save(ss);
        }
    }

    // Huỷ đơn PENDING (thanh toán thất bại/huỷ hoặc hết hạn): xoá booking_seat + đồ ăn để nhả
    // ghế (seat_state vẫn AVAILABLE vì DB không còn cờ HELD), đơn -> CANCELLED. Idempotent.
    @Transactional
    public void cancelBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }
        List<BookingSeat> seats = bookingSeatRepository.findAllByBooking_BookingId(booking.getBookingId())
                .orElse(List.of());
        // Nhả luôn soft hold Redis còn sót (phòng hờ; createBooking thường đã xoá).
        for (BookingSeat bs : seats) {
            redisTemplate.delete(SeatScheduleService.holdKey(bs.getSeatSchedule().getSeatScheduleId()));
        }
        // Xoá booking_seat để giải phóng UNIQUE(seat_schedule_id) cho lần đặt sau.
        bookingSeatRepository.deleteAll(seats);
        bookingFoodAndDrinkRepository.deleteAll(
                bookingFoodAndDrinkRepository.findByBooking_BookingId(booking.getBookingId()));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // Cron: huỷ các đơn PENDING đã quá hạn giữ ghế (expires_at) và nhả ghế.
    @Transactional
    public int cancelExpiredPendingBookings() {
        List<Booking> expired = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING, LocalDateTime.now());
        for (Booking booking : expired) {
            cancelBooking(booking);
        }
        return expired.size();
    }
}
