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
@Table(name = "tbl_amenities")
// tien ich mien phi
public class Amenities extends BaseEntity{

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean deleted = false;

    @ManyToMany
    Set<Homestay> listHomestay;
}
