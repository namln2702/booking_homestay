package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.AmenitiesDTO;
import org.example.do_an_v1.entity.Amenities;

public class AmenitiesMapper {

    public static AmenitiesDTO toDTO(Amenities entity) {
        if (entity == null) return null;
        return AmenitiesDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public static Amenities toEntity(AmenitiesDTO dto) {
        if (dto == null) return null;
        Amenities a = new Amenities();
        a.setId(dto.getId());
        a.setName(dto.getName());
        return a;
    }
}
