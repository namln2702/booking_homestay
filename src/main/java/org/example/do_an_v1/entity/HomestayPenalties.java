package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_homestayPenaties")
public class HomestayPenalties extends BaseEntity{

    @Column(name = "reason")
    private String reason;

    @Column(name = "type")
    private String type ;

    @Column(name = "suspension_util")
    private LocalDateTime suspensionUtil;


    @ManyToOne
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

}
