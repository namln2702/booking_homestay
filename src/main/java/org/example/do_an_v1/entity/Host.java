package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.StatusHost;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "tbl_hosts")
public class Host extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = true )
    private StatusHost statusHost = StatusHost.PENDING;

    @Column(name = "business_name", nullable = true)
    private String businessName;

    @Column(name = "qr_code_url", nullable = true )
    private String qrCodeUrl;

    @Column(name = "role" , nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleUser role = RoleUser.HOST;

    // Tac dong mot bang thi anh huong bang con lai
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    private User user;

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    Set<Homestay> homestays;

}
