package org.example.do_an_v1.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class ImageDTO {
    private Long id;

    private String image_url;

    private Boolean isPrimary ;
}
