package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.configuration.ConfigPayment;
import com.cinemaweb.API.Cinema.Web.dto.response.PaymentResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.TransactionStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @GetMapping("/create_payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request) throws UnsupportedEncodingException {
        long amount = 10000 * 100;
        String vnp_TxnRef = ConfigPayment.getRandomNumber(8);
        String vnp_IpAddr = ConfigPayment.getIpAddress(request);
        String vnp_TmnCode = ConfigPayment.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", ConfigPayment.vnp_Version);
        vnp_Params.put("vnp_Command", ConfigPayment.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other"); // hoặc "billpayment", "topup", ...

        vnp_Params.put("vnp_ReturnUrl", ConfigPayment.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Builing hash data and query string
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
        String paymentUrl = ConfigPayment.vnp_PayUrl + "?" + query;

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("OK");
        paymentResponse.setMessage("Successfully");
        paymentResponse.setURL(paymentUrl);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

    @GetMapping("/payment_info")
    public ResponseEntity<?> transaction(@RequestParam(value = "vnp_Amount") String amount,
                                         @RequestParam(value = "vnp_BankCode") String bankCode,
                                         @RequestParam(value = "vnp_OrderInfo") String order,
                                         @RequestParam(value = "vnp_ResponseCode") String responseCode) {
        TransactionStatus transactionStatus = new TransactionStatus();
        if(responseCode.equals("00")) {
            transactionStatus.setStatus("OK");
            transactionStatus.setMessage("Successfully");
            transactionStatus.setData("");
        } else {
            transactionStatus.setStatus("NO");
            transactionStatus.setMessage("Failed");
            transactionStatus.setData("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(transactionStatus);
    }
}
