package org.example.do_an_v1.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindHomeStayDTO {

    private String city;
    private String state;
    private String begin;
    private String end;
    private Integer numberAdults;
    private Integer numberChildren;
    private Integer numberBaby;
}
