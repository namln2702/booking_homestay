package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.LevelAdmin;
import org.example.do_an_v1.enums.Status;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_admins")
public class Admins extends BaseEntity{


    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false )
    private LevelAdmin levelAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = false )
    private Status status = Status.ACTIVE;


    // Tac dong mot bang thi anh huong bang con lai
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    private Users user;

}
