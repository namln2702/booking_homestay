package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_facilities")
// tien nghi: wifi, dieu hoa, ...
public class Facilities extends BaseEntity{

    @Column(name = "name", nullable = false)
    private String name;// VD: Điều hòa, Máy sấy, Smart TV;

    @Column(name = "category", nullable = false)
    private String category ; // General, Bedroom, Bathroom, Kitchen, Special, View

    @Column(name = "deleted", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean deleted = false;

//    @ManyToMany
//    Set<Homestay> listHomestay;

}
