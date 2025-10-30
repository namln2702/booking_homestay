package org.example.do_an_v1.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;


@Builder
@Getter
public class DailyPriceDVO {
    private Long id;

    private Float price;

    private Boolean isBooked ;

    private Date date;
}
