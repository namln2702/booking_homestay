package org.example.do_an_v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilitiesBatchCreateRequest {

    @NotEmpty(message = "Facilities list cannot be empty")
    @Size(max = 50, message = "Cannot create more than 50 facilities at once")
    @Valid
    private List<FacilitiesCreateRequest> facilities;
}
