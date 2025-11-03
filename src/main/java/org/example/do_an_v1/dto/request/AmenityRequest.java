package org.example.do_an_v1.dto.request;

import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmenityRequest {
    private String name;
    private String description;
    private String imageUrl;
}
