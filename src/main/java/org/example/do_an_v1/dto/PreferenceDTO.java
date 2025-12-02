package org.example.do_an_v1.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceDTO {
    private Long id;

    @Size(max = 100, message = "Preference name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Preference description must be at most 500 characters")
    private String description;
}

