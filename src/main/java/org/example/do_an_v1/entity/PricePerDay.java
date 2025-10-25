package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.Status;

import java.util.Date;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_price_per_days")
public class PricePerDay extends BaseEntity{


    @Column(name = "day", nullable = false, unique = true)
    private Date day ;


    @OneToMany(mappedBy = "pricePerDay")
    Set<HomestayDailyPrice> listHomestayDailyPrice;

}
