package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.DailyPriceDVO;
import org.example.do_an_v1.entity.HomestayDailyPrice;

public class DailyPriceMapper {

    public static DailyPriceDVO toDVO(HomestayDailyPrice entity) {
        if (entity == null) return null;
        return DailyPriceDVO.builder()
                .id(entity.getId())
                .date(entity.getPricePerDay().getDay())
                .price(entity.getPrice())
                .build();
    }

    public static HomestayDailyPrice toEntity(DailyPriceDVO dto) {
        if (dto == null) return null;
        HomestayDailyPrice price = new HomestayDailyPrice();
        price.setId(dto.getId());
        price.setPrice(dto.getPrice());
        return price;
    }
}
