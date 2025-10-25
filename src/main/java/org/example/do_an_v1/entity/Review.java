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
@Table(name = "tbl_reviews")
public class Review extends BaseEntity{

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", nullable = false)
    private String comment ;

    @ManyToOne
    @JoinColumn(name = "homestay_id")
    Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;

    @OneToMany(mappedBy = "review")
    Set<Image> listImage;


}
