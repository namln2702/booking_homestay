package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilitiesCreateRequest {

    @NotBlank(message = "Facility name is required")
    @Size(max = 100, message = "Facility name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Facility category is required")
    @Size(max = 50, message = "Facility category must be at most 50 characters")
    private String category; // General, Bedroom, Bathroom, Kitchen, Special, View
}

