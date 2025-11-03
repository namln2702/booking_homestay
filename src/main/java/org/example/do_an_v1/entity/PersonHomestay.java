package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.TypePerson;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_person_homestay")
public class PersonHomestay extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    Person person;

    @ManyToOne
    @JoinColumn(name = "homestay_id", nullable = false)
    Homestay homestay;

    Integer quantity;


}
