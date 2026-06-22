-- Migration fixes — chạy thủ công trên DB hiện có sau khi cinema.sql đổi schema.

-- OTP quên mật khẩu chuyển sang lưu trong Redis (TTL tự hết hạn, dùng một lần).
-- Bảng MySQL `password_otp` không còn dùng -> bỏ đi.
DROP TABLE IF EXISTS `password_otp`;
