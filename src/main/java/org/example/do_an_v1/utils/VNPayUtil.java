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
     * Tạo payment URL từ VNPay
     */
    public static String createPaymentUrl(VNPayConfig config, String orderId, long amount, 
                                         String orderInfo, String ipAddress) throws Exception {
        String vnp_Version = config.getVersion();
        String vnp_Command = config.getCommand();
        String vnp_TmnCode = config.getTmnCode();
        String vnp_CurrCode = config.getCurrCode();
        String vnp_TxnRef = orderId;
        String vnp_OrderInfo = orderInfo;
        String vnp_OrderType = config.getOrderType();
        long vnp_Amount = amount * 100; // VNPay yêu cầu amount tính bằng xu (x100)
        String vnp_Locale = config.getLocale();
        String vnp_ReturnUrl = config.getReturnUrl();
        String vnp_IpAddr = ipAddress;
        String vnp_CreateDate = getCurrentDateTime();
        String vnp_ExpireDate = getExpireDateTime();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(config.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = config.getUrl() + "?" + queryUrl;
        return paymentUrl;
    }

    /**
     * Xác thực callback từ VNPay
     */
    public static boolean verifyPayment(Map<String, String> params, String secretKey) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        String hashValue = hmacSHA512(secretKey, hashData.toString());
        return hashValue.equals(vnp_SecureHash);
    }

    /**
     * HMAC SHA512
     */
    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmacSHA512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmacSHA512.init(secretKey);
            byte[] digest = hmacSHA512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    /**
     * Lấy IP address từ request
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    /**
     * Lấy thời gian hiện tại theo format yyyyMMddHHmmss
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
     * Lấy thời gian hết hạn (15 phút sau) theo format yyyyMMddHHmmss
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

