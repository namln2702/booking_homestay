package org.example.do_an_v1.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.StatusHost;


@SuperBuilder
@Getter
@Setter
public class HostDTO extends UserDTO{

    private String businessName;

    private String qrCodeUrl;

    private StatusHost status;

}
