package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.StatusHost;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_hosts")
public class Hosts extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = false )
    private StatusHost statusAdmin = StatusHost.PENDING;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "qr_code_url", nullable = true )
    private String qrCodeUrl;

    // Tac dong mot bang thi anh huong bang con lai
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    private Users user;

}
