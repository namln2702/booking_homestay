package org.example.do_an_v1.dto;

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
    private String name;
    private String category; // General, Bedroom, Bathroom, Kitchen, Special, View
}
