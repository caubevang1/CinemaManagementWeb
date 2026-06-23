package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.configuration.ConfigPayment;
import com.cinemaweb.API.Cinema.Web.dto.request.BookingRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingCreateResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.BookingResponse;
import com.cinemaweb.API.Cinema.Web.entity.Payment;
import com.cinemaweb.API.Cinema.Web.service.BookingService;
import com.cinemaweb.API.Cinema.Web.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(@PathVariable String bookingId) {
        return ApiResponse.<BookingResponse>builder()
                .body(bookingService.getBooking(bookingId))
                .build();
    }

    @PostMapping
    public ApiResponse<BookingCreateResponse> createBooking(@RequestBody @Valid BookingRequest bookingRequest,
                                                            HttpServletRequest request) throws UnsupportedEncodingException {
        // Tạo đơn PENDING + giữ ghế + bản ghi payment, rồi dựng URL VNPay để FE redirect.
        Payment payment = bookingService.createBooking(bookingRequest);
        String ipAddr = ConfigPayment.getIpAddress(request);
        String paymentUrl = paymentService.buildPaymentUrl(payment, ipAddr);
        return ApiResponse.<BookingCreateResponse>builder()
                .message("Booking created, awaiting payment")
                .body(BookingCreateResponse.builder()
                        .bookingId(payment.getBooking().getBookingId())
                        .paymentUrl(paymentUrl)
                        .build())
                .build();
    }

    @GetMapping("/myBooking")
    public ApiResponse<List<BookingResponse>> getAllMyBooking() {
        return ApiResponse.<List<BookingResponse>>builder()
                .body(bookingService.getAllMyBooking())
                .build();
    }
}
