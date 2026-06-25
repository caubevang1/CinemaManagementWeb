package com.cinemaweb.API.Cinema.Web.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    // ─────────────── 9xxx: System/Internal Errors ───────────────
    UNKNOWN_EXCEPTION(9000, "Lỗi không xác định!", HttpStatus.INTERNAL_SERVER_ERROR),
    RUNTIME_EXCEPTION(9001, "Lỗi hệ thống!", HttpStatus.INTERNAL_SERVER_ERROR),
    TOO_MANY_REQUESTS(9002, "Bạn thao tác quá nhanh, vui lòng thử lại sau giây lát!", HttpStatus.TOO_MANY_REQUESTS),
    EMAIL_SERVICE_UNAVAILABLE(9003, "Dịch vụ email đang tạm gián đoạn, vui lòng thử lại sau!", HttpStatus.SERVICE_UNAVAILABLE),
    TMDB_UNAVAILABLE(9004, "Dịch vụ dữ liệu phim (TMDB) đang tạm gián đoạn, vui lòng thử lại sau!", HttpStatus.SERVICE_UNAVAILABLE),
    CLOUDINARY_UNAVAILABLE(9005, "Dịch vụ tải ảnh đang quá tải, vui lòng thử lại sau ít phút!", HttpStatus.SERVICE_UNAVAILABLE),
    EMAIL_SEND_FAILED(9006, "Gửi email thất bại, vui lòng thử lại!", HttpStatus.INTERNAL_SERVER_ERROR),
    TMDB_FETCH_FAILED(9007, "Không lấy được dữ liệu phim, vui lòng thử lại!", HttpStatus.INTERNAL_SERVER_ERROR),

    // ─────────────── 1xxx: User-related Errors ───────────────
    USER_EXISTED(1001, "Người dùng đã tồn tại!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTS(1002, "Người dùng không tồn tại!", HttpStatus.BAD_REQUEST),
    LASTNAME_NULL(1003, "Họ không được để trống!", HttpStatus.BAD_REQUEST),
    GENDER_NULL(1004, "Giới tính không được để trống!", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_NULL(1005, "Số điện thoại không được để trống!", HttpStatus.BAD_REQUEST),
    POINT_IS_NULL(1006, "Điểm người dùng không được để trống!", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1007, "Email đã tồn tại!", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1008, "Định dạng email không hợp lệ!", HttpStatus.BAD_REQUEST),

    // ─────────────── 2xxx: Authentication & Authorization ───────────────
    UNAUTHENTICATED(2001, "Chưa xác thực!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "Bạn không có quyền truy cập!", HttpStatus.FORBIDDEN),
    WAIT_OTP(2003, "Vui lòng đợi 90 giây để gửi lại OTP!", HttpStatus.BAD_REQUEST),
    TOKEN_IS_NULL(2004, "Token không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_OTP(2005, "OTP không tồn tại hoặc đã hết hạn!", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(2006,"Token đã hết hạn hoặc bị vô hiệu hóa!" , HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN(2007, "Refresh token không hợp lệ hoặc đã hết hạn!", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REUSED(2008, "Phát hiện tái sử dụng refresh token, phiên đã bị thu hồi!", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED(2009, "Tài khoản hoặc mật khẩu không hợp lệ!", HttpStatus.UNAUTHORIZED),

    // ─────────────── 3xxx: Role & Permission Errors ───────────────
    ROLE_EXISTED(3001, "Vai trò đã tồn tại!", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTS(3002, "Vai trò không tồn tại!", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(3003, "Vai trò không hợp lệ!", HttpStatus.BAD_REQUEST),
    INVALID_PERMISSION(3004, "Quyền không hợp lệ!", HttpStatus.BAD_REQUEST),
    PERMISSION_EXISTED(3005, "Quyền đã tồn tại!", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_NULL(3006, "Tên quyền không được để trống!", HttpStatus.BAD_REQUEST),
    ROLE_NAME_NULL(3007, "Tên vai trò không được để trống!", HttpStatus.BAD_REQUEST),

    // ─────────────── 4xxx: Input Field Validation Errors ───────────────
    USERNAME_IS_NULL(4001, "Tên đăng nhập không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(4002, "Tên đăng nhập phải có ít nhất 6 ký tự!", HttpStatus.BAD_REQUEST),
    PASSWORD_IS_NULL(4003, "Mật khẩu không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(4004, "Mật khẩu phải có ít nhất 8 ký tự!", HttpStatus.BAD_REQUEST),
    EMAIL_IS_NULL(4005, "Email không được để trống!", HttpStatus.BAD_REQUEST),
    DOB_IS_NULL(4006, "Ngày sinh không được để trống!", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_FAIL(4007, "Mật khẩu mới và xác nhận mật khẩu không khớp!", HttpStatus.BAD_REQUEST),

    // ─────────────── 5xxx: Movie-related Errors ───────────────
    MOVIE_EXISTED(5001, "Phim đã tồn tại (trùng tên và ngày phát hành)!", HttpStatus.BAD_REQUEST),
    TMDB_MOVIE_EXISTED(5002, "Phim này đã được import từ TMDB!", HttpStatus.BAD_REQUEST),
    RELEASE_DATE_NULL(5003, "Ngày phát hành không được để trống!", HttpStatus.BAD_REQUEST),
    MOVIE_NOT_EXISTS(5004, "Phim không tồn tại!", HttpStatus.BAD_REQUEST),

    // ─────────────── 6xxx: Booking & Schedule Errors ───────────────
    SEAT_SCHEDULE_NOT_EXISTS(6001, "Ghế của suất chiếu không tồn tại!", HttpStatus.BAD_REQUEST),
    SEAT_ALREADY_BOOKED(6002, "Ghế đã được đặt, vui lòng chọn ghế khác!", HttpStatus.CONFLICT),
    SCHEDULE_TIME_OVERLAP(6003, "Khung giờ chiếu bị trùng với suất chiếu khác trong cùng phòng!", HttpStatus.BAD_REQUEST),
    SEAT_HELD_BY_OTHER(6004, "Ghế đang được giữ bởi người khác!", HttpStatus.CONFLICT),
    HOLD_EXPIRED(6005, "Phiên giữ ghế đã hết hạn, vui lòng chọn lại!", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(6006, "Không tìm thấy giao dịch thanh toán!", HttpStatus.BAD_REQUEST),
    PAYMENT_SIGNATURE_INVALID(6007, "Chữ ký thanh toán không hợp lệ!", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH(6008, "Số tiền thanh toán không khớp!", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_EXISTS(6009, "Đơn đặt vé không tồn tại!", HttpStatus.BAD_REQUEST),

    // ─────────────── 7xxx: Social (Comment & Friendship) Errors ───────────────
    COMMENT_NOT_EXISTS(7001, "Bình luận không tồn tại!", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_OWNER(7002, "Bạn không có quyền với bình luận này!", HttpStatus.FORBIDDEN),
    FRIEND_SELF(7003, "Không thể tự kết bạn với chính mình!", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_EXISTS(7004, "Lời mời kết bạn hoặc quan hệ bạn bè đã tồn tại!", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_EXISTS(7005, "Lời mời kết bạn không tồn tại!", HttpStatus.BAD_REQUEST),
    FRIEND_NOT_AUTHORIZED(7006, "Bạn không có quyền với quan hệ bạn bè này!", HttpStatus.FORBIDDEN),
    CHAT_NOT_FRIENDS(7007, "Chỉ có thể nhắn tin với bạn bè!", HttpStatus.FORBIDDEN),
    MESSAGE_EMPTY(7008, "Nội dung tin nhắn không được để trống!", HttpStatus.BAD_REQUEST),
    TICKET_TRANSFER_NOT_FOUND(7009, "Lời mời chuyển nhượng vé không tồn tại!", HttpStatus.BAD_REQUEST),
    TICKET_NOT_OWNER(7010, "Bạn không sở hữu vé này!", HttpStatus.FORBIDDEN),
    TICKET_TRANSFER_NOT_FRIENDS(7011, "Chỉ có thể chuyển nhượng vé cho bạn bè!", HttpStatus.FORBIDDEN),
    TICKET_SHOW_STARTED(7012, "Suất chiếu đã bắt đầu, không thể chuyển nhượng!", HttpStatus.BAD_REQUEST),
    TICKET_TRANSFER_EXISTS(7013, "Vé này đang có một lời mời chuyển nhượng chờ xử lý!", HttpStatus.BAD_REQUEST),
    TICKET_TRANSFER_SELF(7014, "Không thể tự chuyển nhượng vé cho chính mình!", HttpStatus.BAD_REQUEST),
    TICKET_TRANSFER_NOT_AUTHORIZED(7015, "Bạn không có quyền với lời mời chuyển nhượng này!", HttpStatus.FORBIDDEN),
    TICKET_PIN_NOT_SET(7016, "Bạn chưa thiết lập mã PIN chuyển nhượng!", HttpStatus.BAD_REQUEST),
    TICKET_PIN_INVALID(7017, "Mã PIN không đúng!", HttpStatus.BAD_REQUEST),
    TICKET_PIN_WRONG_CURRENT(7018, "Mã PIN hiện tại không đúng!", HttpStatus.BAD_REQUEST),

    // ─────────────── 8xxx: File Upload Errors ───────────────
    FILE_EMPTY(8001, "File tải lên không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(8002, "Chỉ chấp nhận file ảnh!", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(8003, "Tải ảnh lên thất bại!", HttpStatus.INTERNAL_SERVER_ERROR);

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
