package org.example.do_an_v1.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CodeForEmail {

    private String email;
    private Long userId;
    private Long confirmEmailId;
    private String code;

}
