package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.Status;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_homestay_daily_prices")
public class HomestayDailyPrice extends BaseEntity{

    @Column(name = "price")
    private Float price;

    @Column(name = "isBooked")
    private Boolean isBooked ;

    @ManyToOne
    @JoinColumn(name = "price_per_day_id", nullable = false)
    private PricePerDay pricePerDay;

    @ManyToOne
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

}
