package org.example.do_an_v1.utils;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GenNumber {
    public static String secureRandomNumbers() {
        SecureRandom secureRandom = new SecureRandom();

        String result = IntStream.range(0,6)
                .map(i -> secureRandom.nextInt(10))
                .mapToObj(String:: valueOf)
                .collect(Collectors.joining());

        return result;
    }

    /**
     * Tạo số ngẫu nhiên 8 chữ số cho VNPay orderId (vnp_TxnRef)
     * VNPay yêu cầu vnp_TxnRef chỉ chứa số, không có ký tự đặc biệt
     */
    public static String generateVNPayOrderId() {
        SecureRandom secureRandom = new SecureRandom();
        String result = IntStream.range(0, 8)
                .map(i -> secureRandom.nextInt(10))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
        return result;
    }
}
