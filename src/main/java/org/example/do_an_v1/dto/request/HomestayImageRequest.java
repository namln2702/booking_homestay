package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayImageRequest {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private Boolean isPrimary;
}

