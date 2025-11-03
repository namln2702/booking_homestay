package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.Status;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_amenities")
public class Amenities extends BaseEntity{

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false)
    private String description;

    @Column(name = "image_url", nullable = false)
    private Status imageUrl;

    @ManyToMany
    Set<Homestay> listHomestay;
}
