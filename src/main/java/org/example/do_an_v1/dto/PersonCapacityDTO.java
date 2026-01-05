package org.example.do_an_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.do_an_v1.enums.TypePerson;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonCapacityDTO {
    private TypePerson type;
    private Integer quantity;
}

