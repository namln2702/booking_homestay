package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.FacilitiesDTO;
import org.example.do_an_v1.entity.Facilities;

public class FacilitiesMapper {

    public static FacilitiesDTO toDTO(Facilities entity) {
        if (entity == null) return null;
        return FacilitiesDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .build();
    }

    public static Facilities toEntity(FacilitiesDTO dto) {
        if (dto == null) return null;
        Facilities facility = Facilities.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .build();
        return facility;
    }
}
