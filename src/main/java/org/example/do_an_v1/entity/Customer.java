package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "tbl_customer")
public class Customer extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = true )
    private Status status = Status.ACTIVE;

    @Column(name = "date_of_birth", nullable = true)
    private String dateOfBirth;

    @Column(name = "qr_code_url", nullable = true )
    private String qrCodeUrl;

    @Column(name = "last_booking", nullable = true  )
    private String lastBooking;

    @Column(name = "role" , nullable = true)
    @Enumerated(EnumType.STRING)
    private RoleUser role = RoleUser.CUSTOMER;
    // Tac dong mot bang thi anh huong bang con lai
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    private User user;

    @ManyToMany
    Set<Preference> listPreferences;

    @OneToMany(mappedBy = "customer")
    Set<Review> reviews;

    @OneToMany(mappedBy = "customer")
    Set<Bill> listBill;


}
