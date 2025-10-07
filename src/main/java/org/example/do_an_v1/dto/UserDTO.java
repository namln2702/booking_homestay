package org.example.do_an_v1.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;


@Builder
@Getter
@Setter
public class UserDTO {
    private String username;

    private String password;

    private String email;

    private String phone;

    private String name;
    private Integer age;

    private RoleUser roleUser;

    private Boolean isOnline;

    private String avatarUrl;

    private String googleId;

    private Status emailVerify;
}
