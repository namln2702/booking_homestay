package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusBill;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_bills")
public class Bill extends BaseEntity{

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "check_in", nullable = false)
    private LocalDateTime checkIn ;

    @Column(name = "check_out", nullable = false)
    private LocalDateTime checkOut ;

    @Column(name = "status", nullable = false)
    private StatusBill status;

    @Column(name = "actual_checkin_time")
    private LocalDateTime actualCheckinTime;

    @ManyToOne
    @JoinColumn(name = "homestay_id")
    Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;

    @ManyToOne
    @JoinColumn(name = "customer_booking_info_id")
    CustomerBookingInfo customerBookingInfo;

    @OneToMany(mappedBy = "bill")
    Set<Transaction> listTransaction;


}
