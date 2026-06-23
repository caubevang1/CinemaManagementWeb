package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.PaymentConfirmResponse;
import com.cinemaweb.API.Cinema.Web.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Frontend (route /payment-result) gọi sau khi VNPay redirect người dùng về, kèm toàn bộ
    // tham số vnp_*. Backend xác thực chữ ký HMAC rồi cập nhật trạng thái đơn/giao dịch.
    @GetMapping("/vnpay-return")
    public ApiResponse<PaymentConfirmResponse> vnpayReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });
        return ApiResponse.<PaymentConfirmResponse>builder()
                .body(paymentService.confirmReturn(params, request.getQueryString()))
                .build();
    }

    // IPN: VNPay gọi server-to-server (GET, query vnp_*) — nguồn xác nhận thanh toán đáng tin.
    // Trả thẳng JSON {"RspCode","Message"} theo đặc tả VNPay (KHÔNG bọc ApiResponse), để VNPay
    // biết đã nhận; nếu khác 00 hoặc không phản hồi, VNPay sẽ gọi lại. Không kèm JWT (permitAll).
    @GetMapping("/vnpay-ipn")
    public Map<String, String> vnpayIpn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });
        return paymentService.confirmIpn(params, request.getQueryString());
    }
}
