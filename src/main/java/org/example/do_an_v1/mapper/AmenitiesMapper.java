package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.AmenitiesDTO;
import org.example.do_an_v1.entity.Amenities;

public class AmenitiesMapper {

    public static AmenitiesDTO toDTO(Amenities entity) {
        if (entity == null) return null;
        return AmenitiesDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .build();
    }

    public static Amenities toEntity(AmenitiesDTO dto) {
        if (dto == null) return null;
        return Amenities.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}
