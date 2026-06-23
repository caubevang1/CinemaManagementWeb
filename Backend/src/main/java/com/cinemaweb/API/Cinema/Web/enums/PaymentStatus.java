package com.cinemaweb.API.Cinema.Web.enums;

// Trạng thái giao dịch thanh toán (payment.status).
// PENDING: đã tạo lệnh, chờ kết quả từ VNPay.
// SUCCESS: VNPay báo thành công và chữ ký hợp lệ.
// FAILED: VNPay báo thất bại/huỷ hoặc xác thực không hợp lệ.
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
