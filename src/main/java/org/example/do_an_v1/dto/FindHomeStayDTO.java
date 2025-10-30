package org.example.do_an_v1.dto;


import lombok.Getter;

@Getter
public class FindHomeStayDTO {

    private String address;
    private String begin;
    private String end;
    private Integer numberAdults;
    private Integer numberChildren;
    private Integer numberBaby;
}
