package org.example.do_an_v1.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHost;

import java.util.List;


@SuperBuilder
@Getter
@Setter
public class UserDTO {
    private String username;

    private String password;

    private String email;

    private String phone;

    private String name;

    private Integer age;

    private Boolean isOnline;

    private Status statusAdmin;
    private StatusHost statusHost;
    private Status statusCustomer;

    private String avatarUrl;

    private String googleId;
    private List<String> role;

}
