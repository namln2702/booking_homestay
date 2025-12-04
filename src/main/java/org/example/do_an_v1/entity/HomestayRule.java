package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.RuleTypeHomestay;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_homestay_rules")
public class HomestayRule extends BaseEntity{

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type" )
    private RuleTypeHomestay ruleTypeHomestay ;

    @ManyToOne
    @JoinColumn(name = "home_id", nullable = false)
    private Homestay homestay;

}
