package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.Status;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_homestay_image")
public class HomestayImage extends BaseEntity{

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary ;

    @ManyToOne
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

}
