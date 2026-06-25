package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.configuration.ConfigPayment;
import com.cinemaweb.API.Cinema.Web.dto.response.PaymentConfirmResponse;
import com.cinemaweb.API.Cinema.Web.entity.Booking;
import com.cinemaweb.API.Cinema.Web.entity.Payment;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.enums.PaymentStatus;
import com.cinemaweb.API.Cinema.Web.event.BookingPaidEvent;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Dựng URL thanh toán VNPay cho một giao dịch (amount lấy từ payment, không hard-code).
    public String buildPaymentUrl(Payment payment, String ipAddr) throws UnsupportedEncodingException {
        // VNPay nhận số tiền theo đơn vị "xu" (x100), không thập phân.
        long amount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", ConfigPayment.vnp_Version);
        vnp_Params.put("vnp_Command", ConfigPayment.vnp_Command);
        vnp_Params.put("vnp_TmnCode", ConfigPayment.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_TxnRef", payment.getTxnRef());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don dat ve: " + payment.getBooking().getBookingId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_ReturnUrl", ConfigPayment.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddr);

        // vnp_ExpireDate = đúng hạn giữ ghế (booking.expiresAt) -> trang VNPay tự đếm ngược về
        // cùng mốc với đồng hồ ở trang ghế/combo (một timer duy nhất). Dùng giờ server (GMT+7),
        // khớp với LocalDateTime.now() mà toàn bộ luồng giữ ghế đang dùng.
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // Mốc tạo giao dịch lấy đúng từ payment.createdAt (thời điểm tạo đơn) thay vì now(), để
        // CreateDate khớp với đồng hồ giữ ghế kể cả khi URL được dựng lại sau đó. Fallback now()
        // nếu vì lý do nào đó đơn chưa có createdAt.
        LocalDateTime createdAt = payment.getCreatedAt() != null ? payment.getCreatedAt() : LocalDateTime.now();
        vnp_Params.put("vnp_CreateDate", createdAt.format(fmt));
        LocalDateTime expire = payment.getBooking().getExpiresAt();
        if (expire == null || !expire.isAfter(createdAt)) {
            expire = createdAt.plusMinutes(15); // fallback an toàn nếu đơn chưa có hạn
        }
        vnp_Params.put("vnp_ExpireDate", expire.format(fmt));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Iterator<String> it = fieldNames.iterator(); it.hasNext(); ) {
            String fieldName = it.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append("=").append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (it.hasNext()) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }
        String vnp_SecureHash = ConfigPayment.hmacSHA512(ConfigPayment.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        return ConfigPayment.vnp_PayUrl + "?" + query;
    }

    // Return URL: frontend gọi sau khi VNPay redirect user về — chỉ dùng để HIỂN THỊ kết quả.
    // Không còn là nguồn chân lý (IPN mới là nguồn xác nhận tiền đáng tin), nhưng vẫn cập nhật
    // trạng thái nếu IPN chưa tới kịp. Idempotent nhờ kiểm tra payment.status != PENDING.
    @Transactional
    public PaymentConfirmResponse confirmReturn(Map<String, String> params, String rawQuery) {
        if (!signatureValidRaw(rawQuery, params)) {
            throw new AppException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        Payment payment = paymentRepository.findByTxnRef(params.get("vnp_TxnRef"))
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        Booking booking = payment.getBooking();

        // Idempotent: nếu đã xử lý rồi (vd IPN đã chốt trước) thì trả về trạng thái hiện tại.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return PaymentConfirmResponse.builder()
                    .success(payment.getStatus() == PaymentStatus.SUCCESS)
                    .bookingId(booking.getBookingId())
                    .status(booking.getStatus().name())
                    .message("Giao dịch đã được xử lý trước đó.")
                    .build();
        }

        if (!amountMatches(payment, params)) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        boolean success = applyResult(payment, params);
        if (success) {
            return PaymentConfirmResponse.builder()
                    .success(true).bookingId(booking.getBookingId())
                    .status("PAID").message("Thanh toán thành công.").build();
        }
        return PaymentConfirmResponse.builder()
                .success(false).bookingId(booking.getBookingId())
                .status("CANCELLED").message("Thanh toán thất bại hoặc bị huỷ.").build();
    }

    // IPN (Instant Payment Notification): VNPay gọi server-to-server, KHÔNG kèm JWT. Đây là nguồn
    // xác nhận tiền đáng tin (VNPay retry tới khi nhận được RspCode=00). Trả JSON {RspCode,Message}
    // đúng đặc tả VNPay thay vì ném exception, để VNPay biết đã nhận hay cần gọi lại.
    @Transactional
    public Map<String, String> confirmIpn(Map<String, String> params, String rawQuery) {
        if (!signatureValidRaw(rawQuery, params)) {
            return ipnResponse("97", "Invalid Checksum");
        }

        Payment payment = paymentRepository.findByTxnRef(params.get("vnp_TxnRef")).orElse(null);
        if (payment == null) {
            return ipnResponse("01", "Order not Found");
        }
        if (!amountMatches(payment, params)) {
            return ipnResponse("04", "Invalid Amount");
        }
        // Idempotent: VNPay có thể gọi IPN nhiều lần — đã chốt rồi thì báo confirmed, không xử lý lại.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return ipnResponse("02", "Order already confirmed");
        }

        applyResult(payment, params);
        return ipnResponse("00", "Confirm Success");
    }

    // Cập nhật Payment + Booking theo kết quả VNPay, dùng chung cho cả return URL lẫn IPN.
    // Chỉ được gọi khi payment đang PENDING (đã kiểm ở caller) -> publish event đúng một lần cho
    // lần chuyển PENDING->SUCCESS đầu tiên, bất kể return hay IPN tới trước. Trả về có thành công không.
    private boolean applyResult(Payment payment, Map<String, String> params) {
        Booking booking = payment.getBooking();
        payment.setResponseCode(params.get("vnp_ResponseCode"));
        payment.setBankCode(params.get("vnp_BankCode"));
        payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));

        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.getOrDefault("vnp_TransactionStatus", "00"));

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
            bookingService.confirmBookingPaid(booking);
            // Side-effect (gửi email vé...) được tách ra worker qua RabbitMQ. Publish domain event;
            // một @TransactionalEventListener(AFTER_COMMIT) mới đẩy message khi DB đã commit PAID.
            User user = booking.getUser();
            eventPublisher.publishEvent(new BookingPaidEvent(
                    booking.getBookingId(), payment.getTxnRef(),
                    user != null ? user.getEmail() : null));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            bookingService.cancelBooking(booking);
        }
        return success;
    }

    // Xác thực chữ ký trên RAW query string (đúng bytes VNPay đã ký) — KHÔNG decode rồi encode lại,
    // nên tránh hẳn lệch +/%20/charset (nguyên nhân RspCode 97 khi tool VNPay ký bằng %20). Nếu vì
    // lý do gì không có raw query thì fallback về cách verify dựa trên Map đã parse.
    private boolean signatureValidRaw(String rawQuery, Map<String, String> params) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return signatureValid(params);
        }
        String received = null;
        // TreeMap: sort key theo natural order, khớp Collections.sort của VNPay.
        Map<String, String> encoded = new java.util.TreeMap<>();
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) continue;
            String key = pair.substring(0, idx);
            String value = pair.substring(idx + 1); // GIỮ NGUYÊN value đã encode
            if ("vnp_SecureHash".equals(key)) {
                received = value;
            } else if (!"vnp_SecureHashType".equals(key)) {
                encoded.put(key, value);
            }
        }
        StringBuilder hashData = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> it = encoded.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> e = it.next();
            hashData.append(e.getKey()).append('=').append(e.getValue());
            if (it.hasNext()) {
                hashData.append('&');
            }
        }
        String computed = ConfigPayment.hmacSHA512(ConfigPayment.secretKey, hashData.toString());
        return received != null && received.equalsIgnoreCase(computed);
    }

    // Xác thực chữ ký HMAC (fallback): vnp_SecureHash tính trên Map đã parse (đã sort, re-encode value).
    private boolean signatureValid(Map<String, String> params) {
        Map<String, String> fields = new HashMap<>(params);
        String receivedHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        return receivedHash != null && receivedHash.equalsIgnoreCase(buildReturnHash(fields));
    }

    // Đối chiếu số tiền VNPay trả về (đơn vị xu, x100) với amount của đơn.
    private boolean amountMatches(Payment payment, Map<String, String> params) {
        long expected = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact();
        long received = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        return expected == received;
    }

    private Map<String, String> ipnResponse(String code, String message) {
        Map<String, String> resp = new HashMap<>();
        resp.put("RspCode", code);
        resp.put("Message", message);
        return resp;
    }

    // Tính lại HMAC-SHA512 trên các field (đã sort, URL-encode value) để so với vnp_SecureHash.
    private String buildReturnHash(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (Iterator<String> it = fieldNames.iterator(); it.hasNext(); ) {
            String fieldName = it.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (it.hasNext()) {
                    hashData.append("&");
                }
            }
        }
        return ConfigPayment.hmacSHA512(ConfigPayment.secretKey, hashData.toString());
    }
}
