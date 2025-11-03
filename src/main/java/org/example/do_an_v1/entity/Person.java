package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.TypePerson;
import org.springframework.format.annotation.NumberFormat;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_persons")
public class Person extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypePerson type;

    @OneToMany(mappedBy = "person")
    Set<PersonHomestay> lisPersonHomestay;

}
