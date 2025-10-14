package org.example.do_an_v1.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.Status;


@SuperBuilder
@Getter
@Setter
public class CustomerDTO extends UserDTO{

    private Status status;

    private String dateOfBirth;

    private String qrCodeUrl;

    private String lastBooking;

}
