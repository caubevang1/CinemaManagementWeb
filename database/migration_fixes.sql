-- Migration fixes — chạy thủ công trên DB hiện có sau khi cinema.sql đổi schema.

-- OTP quên mật khẩu chuyển sang lưu trong Redis (TTL tự hết hạn, dùng một lần).
-- Bảng MySQL `password_otp` không còn dùng -> bỏ đi.
DROP TABLE IF EXISTS `password_otp`;

-- ─────────────────────────────────────────────────────────────────────────────
-- Phần 1: Thanh toán PENDING -> PAID (booking.status + bảng payment)
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE `booking`
    ADD COLUMN `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
        NOT NULL DEFAULT 'PENDING';
-- Vé đã tồn tại trước khi có cột status coi như đã thanh toán xong.
UPDATE `booking` SET `status` = 'PAID' WHERE `status` = 'PENDING';

CREATE TABLE IF NOT EXISTS `payment` (
    `payment_id` int NOT NULL AUTO_INCREMENT,
    `booking_id` int NOT NULL,
    `txn_ref` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `amount` decimal(12, 2) NOT NULL,
    `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
    `method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'VNPAY',
    `bank_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `vnp_transaction_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `response_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `created_at` datetime NOT NULL,
    `paid_at` datetime DEFAULT NULL,
    PRIMARY KEY (`payment_id`),
    UNIQUE KEY `uq_payment_txn_ref` (`txn_ref`),
    KEY `idx_payment_booking` (`booking_id`),
    CONSTRAINT `fk_payment_booking` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- Phần 2: Siết NOT NULL cho các FK của booking_seat / bookingfoodanddrink.
-- (Bảng đang trống nên không cần dọn dữ liệu NULL trước.)
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE `booking_seat`
    MODIFY `booking_id` int NOT NULL,
    MODIFY `seat_schedule_id` int NOT NULL,
    MODIFY `price` decimal(12, 2) NOT NULL;
ALTER TABLE `bookingfoodanddrink`
    MODIFY `booking_id` int NOT NULL,
    MODIFY `fd_id` int NOT NULL,
    MODIFY `quantity` int NOT NULL,
    MODIFY `price` decimal(12, 2) NOT NULL;

-- ─────────────────────────────────────────────────────────────────────────────
-- Phần 3: Chuẩn hoá cột tham chiếu user về varchar(36) + thêm FK chat_message.transfer_id.
-- Cột cha user.user_id đã là varchar(36); thu hẹp cột con cho khớp (UUID = 36 ký tự).
-- MySQL cho phép MODIFY cột con của FK khi kiểu vẫn tương thích, không cần drop FK.
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE `booking`         MODIFY `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE `comment`         MODIFY `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `friendship`      MODIFY `requester_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              MODIFY `addressee_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `chat_message`    MODIFY `sender_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              MODIFY `recipient_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `ticket_transfer` MODIFY `from_user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              MODIFY `to_user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

-- FK còn thiếu: chat_message.transfer_id -> ticket_transfer (tin nhắn loại TRANSFER).
ALTER TABLE `chat_message`
    ADD KEY `fk_chat_transfer` (`transfer_id`),
    ADD CONSTRAINT `fk_chat_transfer` FOREIGN KEY (`transfer_id`)
        REFERENCES `ticket_transfer` (`transfer_id`) ON DELETE SET NULL;

-- ─────────────────────────────────────────────────────────────────────────────
-- Phần 4: Giữ ghế chuyển sang Redis (TTL tự hết hạn).
-- - DB seat_state chỉ còn AVAILABLE / BOOKED -> bỏ cột held_until, held_by_user_id (+ FK).
-- - booking.expires_at = hạn chót giữ ghế (heldUntil), thay cho việc suy từ booking_day.
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE `seat_schedule` DROP FOREIGN KEY `fk_seat_schedule_held_by`;
ALTER TABLE `seat_schedule` DROP KEY `fk_seat_schedule_held_by_idx`;
ALTER TABLE `seat_schedule`
    DROP COLUMN `held_until`,
    DROP COLUMN `held_by_user_id`;
-- Mọi ghế đang HELD ở DB (nếu có) coi như nhả về AVAILABLE; HELD nay chỉ tồn tại trong Redis.
UPDATE `seat_schedule` SET `seat_state` = 'AVAILABLE' WHERE `seat_state` = 'HELD';

ALTER TABLE `booking` ADD COLUMN `expires_at` datetime DEFAULT NULL;
