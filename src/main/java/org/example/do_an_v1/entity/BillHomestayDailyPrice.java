package org.example.do_an_v1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_bill_homestay_daily_prices")
public class BillHomestayDailyPrice extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne
    @JoinColumn(name = "homestay_daily_price_id", nullable = false)
    private HomestayDailyPrice homestayDailyPrice;

    @Column(name = "day")
    private Date day;

    @Column(name = "price")
    private Float price;
}

