package org.example.do_an_v1.dto;


import lombok.*;

@Builder
@Getter
@Setter
public class CodeForEmail {

    private String email;
    private Long userId;
    private Long confirmEmailId;
    private String code;

}
