package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
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


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusBill status;

    @Column(name = "actual_checkin_time")
    private LocalDateTime actualCheckinTime;

    @Column(name = "actual_checkout_time")
    private LocalDateTime actualCheckoutTime;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private java.math.BigDecimal totalAmount; // Tổng giá trị bill (100%)

    @ManyToOne
    @JoinColumn(name = "homestay_id")
    Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;

    @ManyToOne
    @JoinColumn(name = "customer_booking_info_id")
    CustomerBookingInfo customerBookingInfo;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<BillGuest> guestAllocations;

    @OneToMany(mappedBy = "bill")
    Set<Transaction> listTransaction;

    @OneToMany(mappedBy = "bill")
    Set<HomestayDailyPrice> listHomestayDailyPrices;

    @OneToMany(mappedBy = "bill")
    Set<Complaint> listComplaint;


}
