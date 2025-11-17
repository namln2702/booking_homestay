package org.example.do_an_v1.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Date {

    public static String DateToString(LocalDateTime time){
        try{
            return time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }catch (Exception e){
            throw new RuntimeException("String not validate");
        }

    }

    public static Integer compareDate(LocalDateTime a, LocalDateTime b) {
        if (a == null || b == null) return null;

        // So sánh chỉ theo ngày, bỏ qua giờ phút giây
        if (a.toLocalDate().isBefore(b.toLocalDate())) {
            return -1; // a < b
        } else if (a.toLocalDate().isAfter(b.toLocalDate())) {
            return 1;  // a > b
        } else {
            return 0;  // a = b
        }
    }
}
