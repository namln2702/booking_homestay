package org.example.do_an_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing

public class DoAnV1Application {

    public static void main(String[] args) {
        SpringApplication.run(DoAnV1Application.class, args);
    }

}
