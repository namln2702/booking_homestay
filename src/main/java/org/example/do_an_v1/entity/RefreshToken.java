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
@Table(name = "tbl_tokens")
public class RefreshToken extends BaseEntity{

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "status", nullable = false)
    private Status statusToken ;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
