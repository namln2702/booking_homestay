package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_users")
public class Users extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true )
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleUser roleUser;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline;

    @Column(name = "avatar_url", nullable = true)
    private String avatarUrl;

    @Column(name = "age" , nullable = false)
    private Integer age;

    @Column(name = "name" , nullable = false)
    private String name;

    @Column(name = "google_id", nullable = false)
    private String googleId;


    @Column(name = "email_verify", nullable = false)
    private Boolean emailVerify;

    @OneToOne(mappedBy = "user")
    private Admins admin;

    @OneToOne(mappedBy = "user")
    private Customers customer;

    @OneToOne(mappedBy = "user")
    private Hosts host;

    @OneToMany(mappedBy = "user")
    private Set<Token> tokens;

    @ManyToMany
    private Set<Preferences> preferences;
}
