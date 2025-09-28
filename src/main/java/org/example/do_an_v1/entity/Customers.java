package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.StatusCustomer;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_customers")
public class Customers extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = false )
    private StatusCustomer statusAdmin = StatusCustomer.ACTIVE;

    @Column(name = "date_of_birth", nullable = false)
    private String dateOfBirth;

    @Column(name = "qr_code_url", nullable = true )
    private String qrCodeUrl;

    @Column(name = "last_booking", nullable = true  )
    private String lastBooking;

    // Tac dong mot bang thi anh huong bang con lai
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    private Users user;


}
