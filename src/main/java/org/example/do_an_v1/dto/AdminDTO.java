package org.example.do_an_v1.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.LevelAdmin;
import org.example.do_an_v1.enums.Status;


@SuperBuilder
@Getter
@Setter
public class AdminDTO extends UserDTO {

    private LevelAdmin levelAdmin;

    private Status status;

}
