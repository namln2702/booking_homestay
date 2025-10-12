package org.example.do_an_v1.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.Status;


@SuperBuilder
@Getter
@Setter
public class HostDTO extends UserDTO{

    private String businessName;

    private String qr_code_url;

    private Status status;

}
