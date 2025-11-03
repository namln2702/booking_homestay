package org.example.do_an_v1.dto.request;

import lombok.*;

import java.util.Date;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayDailyPriceRequest {
    private Date day;
    private Float price;
}
