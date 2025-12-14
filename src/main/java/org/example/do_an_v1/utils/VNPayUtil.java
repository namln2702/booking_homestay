package org.example.do_an_v1.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.example.do_an_v1.configuration.VNPayConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtil {

    /**
     * Tạo payment URL VNPay
     */
    public static String createPaymentUrl(
            VNPayConfig config,
            String orderId,
            long amount,
            String orderInfo,
            String ipAddress
    ) throws Exception {

        Map<String, String> vnpParams = new HashMap<>();

        vnpParams.put("vnp_Version", config.getVersion());
        vnpParams.put("vnp_Command", config.getCommand());
        vnpParams.put("vnp_TmnCode", config.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // x100
        vnpParams.put("vnp_CurrCode", config.getCurrCode());
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", config.getOrderType());
        vnpParams.put("vnp_Locale", config.getLocale());
        vnpParams.put("vnp_ReturnUrl", config.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_CreateDate", getCurrentDateTime());
        vnpParams.put("vnp_ExpireDate", getExpireDateTime());

        // Sort params
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        // Lọc các field có giá trị hợp lệ trước
        List<String> validFields = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                validFields.add(fieldName);
            }
        }

        // Build hashData và query string
        for (int i = 0; i < validFields.size(); i++) {
            String fieldName = validFields.get(i);
            String fieldValue = vnpParams.get(fieldName);

            // ✅ hashData: PHẢI encode giá trị theo US_ASCII (theo yêu cầu VNPay)
            hashData.append(fieldName)
                    .append('=')
//                    .append(fieldValue);
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

            // ✅ query: encode cả tên và giá trị theo UTF-8
            query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

            // Thêm & nếu không phải field cuối cùng
            if (i < validFields.size() - 1) {
                hashData.append('&');
                query.append('&');
            }
        }

        String secureHash = hmacSHA512(config.getSecretKey(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return config.getUrl() + "?" + query;
    }

    /**
     * Verify callback / IPN từ VNPay
     * 
     * Theo code mẫu VNPay:
     * 1. Lấy tất cả parameters (trừ vnp_SecureHash và vnp_SecureHashType)
     * 2. Chỉ lấy các field có giá trị không null và không empty
     * 3. Sort field names
     * 4. Build hashData: fieldName=fieldValue (encode theo US_ASCII) & fieldName2=fieldValue2...
     * 5. Tính HMAC SHA512 và so sánh với vnp_SecureHash
     */
    public static boolean verifyPayment(Map<String, String> rawParams, String secretKey) {

        String vnpSecureHash = rawParams.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            return false;
        }

        // Copy map để không ảnh hưởng dữ liệu gốc
        Map<String, String> params = new HashMap<>(rawParams);
        
        // Loại bỏ vnp_SecureHash và vnp_SecureHashType (theo code mẫu VNPay)
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // Lọc các field có giá trị hợp lệ (theo code mẫu: if (fieldValue != null && fieldValue.length() > 0))
        List<String> validFields = new ArrayList<>();
        for (String fieldName : params.keySet()) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                validFields.add(fieldName);
            }
        }

        // Sort field names (theo code mẫu VNPay)
        Collections.sort(validFields);

        // Build hashData - giống như khi tạo payment URL
        StringBuilder hashData = new StringBuilder();
        
        for (int i = 0; i < validFields.size(); i++) {
            String fieldName = validFields.get(i);
            String fieldValue = params.get(fieldName);

            try {
                // Build hashData: fieldName=fieldValue (encode theo US_ASCII)
                // Giống như khi tạo payment URL
                hashData.append(fieldName)
                        .append('=')
                        .append(fieldValue);
//                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                // Thêm & nếu không phải field cuối cùng
                if (i < validFields.size() - 1) {
                    hashData.append('&');
                }
            } catch (Exception e) {
                return false;
            }
        }

        // Tính hash và so sánh (theo code mẫu: signValue.equals(vnp_SecureHash))
        String hashDataString = hashData.toString();
        String calculatedHash = hmacSHA512(secretKey, hashDataString);
        
        // Log để debug (có thể bỏ sau khi test xong)
        System.out.println("=== VNPay Verify Debug ===");
        System.out.println("HashData: " + hashDataString);
        System.out.println("Calculated Hash: " + calculatedHash);
        System.out.println("Received Hash: " + vnpSecureHash);
        System.out.println("Match: " + calculatedHash.equalsIgnoreCase(vnpSecureHash));
        
        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }

    /**
     * HMAC SHA512
     */
    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);

            byte[] raw = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();

            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA512 error", e);
        }
    }

    /**
     * Lấy IP thật
     */
    public static String getIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0];
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * yyyyMMddHHmmss
     */
    private static String getCurrentDateTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return String.format("%04d%02d%02d%02d%02d%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    }

    /**
     * Expire sau 15 phút
     */
    private static String getExpireDateTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        cal.add(Calendar.MINUTE, 15);
        return String.format("%04d%02d%02d%02d%02d%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    }
}
