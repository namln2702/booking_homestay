package org.example.do_an_v1.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import org.example.do_an_v1.enums.Status;


@Builder
@Getter
public class AmenitiesDTO {

    private Long id;

    private String name;

    private Status description;

    private Status imageUrl;
}
