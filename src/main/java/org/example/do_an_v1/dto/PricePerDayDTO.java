package org.example.do_an_v1.dto;


import lombok.Getter;

import java.util.Date;

@Getter
public class PricePerDayDTO {

    private Long id;
    private Float price;
    private Date day;

}
