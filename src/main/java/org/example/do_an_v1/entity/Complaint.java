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
@Table(name = "tbl_complaints")
public class Complaint extends BaseEntity{


    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @OneToMany(mappedBy = "complaint")
    Set<Image> listImage;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;
}
