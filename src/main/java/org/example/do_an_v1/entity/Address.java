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
    private String addressLine;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;


    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "url")
    private String url;

    @OneToOne(mappedBy = "address")
    Homestay homestay;
}
