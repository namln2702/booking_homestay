package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.StatusToken;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_tokens")
public class Token extends BaseEntity{

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "status", nullable = false)
    private StatusToken statusToken ;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

}
