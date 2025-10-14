package org.example.do_an_v1.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AccessTokenSystemDTO<T> {
    private String token;
    private T user;
}
