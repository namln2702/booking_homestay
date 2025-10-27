package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "tbl_users")
public class User extends BaseEntity {

    @Column(name = "username", nullable = true, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true )
    private String email;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "is_online", nullable = true)
    private Boolean isOnline;

    @Column(name = "avatar_url", nullable = true)
    private String avatarUrl;

    @Column(name = "age" , nullable = true)
    private Integer age;

    @Column(name = "name" , nullable = true)
    private String name;

    @Column(name = "google_id", nullable = true)
    private String googleId;

    @OneToOne(mappedBy = "user")
    private Admin admin;

    @OneToOne(mappedBy = "user")
    private Customer customer;

    @OneToOne(mappedBy = "user")
    private Host host;

    @OneToMany(mappedBy = "user")
    private Set<Token> tokens;

    @ManyToMany
    private Set<Preference> preferences;

    @OneToMany(mappedBy = "fromUser")
    Set<Transaction> listTransactionForFromUser;

    @OneToMany(mappedBy = "toUser")
    Set<Transaction> listTransactionForToUser;
}
