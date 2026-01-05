package org.example.do_an_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayDailyPriceDTO {
    private Long id;
    private Date day;
    private Float price;
    private Boolean booked;
    private Boolean activeHost;
}

