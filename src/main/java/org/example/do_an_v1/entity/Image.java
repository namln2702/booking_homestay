package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_images")
public class Image extends BaseEntity{

    @Column(name = "image_url")
    private String image_url;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary ;

    @ManyToOne
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

}
