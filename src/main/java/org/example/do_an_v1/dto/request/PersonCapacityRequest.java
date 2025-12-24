package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.enums.TypePerson;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonCapacityRequest {

    @NotNull(message = "type is required")
    private TypePerson type;

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity must be greater or equal 0")
    private Integer quantity;
}
