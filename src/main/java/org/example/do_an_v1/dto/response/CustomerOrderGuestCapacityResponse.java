package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.enums.TypePerson;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderGuestCapacityResponse {
    private TypePerson type;
    private Integer quantity;
}
