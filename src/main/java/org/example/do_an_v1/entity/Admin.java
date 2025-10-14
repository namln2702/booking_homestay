package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.LevelAdmin;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "tbl_admins")
public class Admin extends BaseEntity{


    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = true )
    private LevelAdmin levelAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = true )
    private Status status = Status.ACTIVE;

    @Column(name = "role" , nullable = true)
    @Enumerated(EnumType.STRING)
    private RoleUser role = RoleUser.ADMIN;


    // Tac dong mot bang thi anh huong bang con lai
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    private User user;

}
