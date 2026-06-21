package com.cinemaweb.API.Cinema.Web.enums;

// Trạng thái ghế trong 1 suất chiếu (seat_schedule.seat_state).
// AVAILABLE: còn trống. HELD: đang giữ tạm khi user thanh toán (kèm held_until).
// BOOKED: đã thanh toán xong. Thay cho boolean cũ (chỉ có trống/đã đặt).
public enum SeatState {
    AVAILABLE,
    HELD,
    BOOKED
}
