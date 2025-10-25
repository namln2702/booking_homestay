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

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "status", nullable = false)
    private Status statusToken ;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @OneToMany(mappedBy = "complaint")
    Set<Image> listImage;

}
