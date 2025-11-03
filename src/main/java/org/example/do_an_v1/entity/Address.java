package org.example.do_an_v1.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "tbl_addresses")
public class Address extends BaseEntity {

    @Column(name = "address_line")
    private String address_line;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    // Vĩ độ
    @Column(name = "latitude", length = 50)
    private String latitude;

    // Kinh độ
    @Column(name = "longitude", length = 50)
    private String longitude;

    @OneToOne(mappedBy = "address")
    Homestay homestay;
}
