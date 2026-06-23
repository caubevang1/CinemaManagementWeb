package com.cinemaweb.API.Cinema.Web.enums;

// Trạng thái đơn đặt vé (booking.status).
// PENDING: vừa tạo, đang chờ thanh toán (ghế đang HELD).
// PAID: đã thanh toán thành công (ghế chuyển BOOKED).
// CANCELLED: thanh toán thất bại/huỷ hoặc hết hạn giữ ghế (ghế nhả về AVAILABLE).
public enum BookingStatus {
    PENDING,
    PAID,
    CANCELLED
}
