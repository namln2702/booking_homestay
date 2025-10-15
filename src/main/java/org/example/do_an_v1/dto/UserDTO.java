package org.example.do_an_v1.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;


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

    private String avatarUrl;

    private String googleId;

}
