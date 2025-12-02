package org.example.do_an_v1.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilitiesDTO {
    private Long id;

    @Size(max = 100, message = "Facility name must be at most 100 characters")
    private String name;

    @Size(max = 50, message = "Facility category must be at most 50 characters")
    private String category; // General, Bedroom, Bathroom, Kitchen, Special, View
}
