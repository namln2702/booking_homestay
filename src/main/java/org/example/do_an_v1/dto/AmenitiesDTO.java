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
public class AmenitiesDTO {

    private Long id;

    @Size(max = 100, message = "Amenity name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 255, message = "Image URL must be at most 255 characters")
    private String imageUrl;
}
