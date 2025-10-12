package org.example.do_an_v1.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;


@SuperBuilder
@Getter
@Setter
public class CustomerDTO extends UserDTO{

}
