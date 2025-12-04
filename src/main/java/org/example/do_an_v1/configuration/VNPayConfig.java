package org.example.do_an_v1.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

//    @Value("${vnpay.tmn-code}")
    private String tmnCode;

//    @Value("${vnpay.secret-key}")
    private String secretKey;

//    @Value("${vnpay.url}")
    private String url;

//    @Value("${vnpay.return-url}")
    private String returnUrl;

//    @Value("${vnpay.ipn-url}")
    private String ipnUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.order-type}")
    private String orderType;

    @Value("${vnpay.locale}")
    private String locale;

    @Value("${vnpay.curr-code}")
    private String currCode;
}

