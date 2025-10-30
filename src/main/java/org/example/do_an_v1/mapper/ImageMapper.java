package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.ImageDTO;
import org.example.do_an_v1.entity.Image;

public class ImageMapper {

    public static ImageDTO toDTO(Image entity) {
        if (entity == null) return null;
        return ImageDTO.builder()
                .id(entity.getId())
                .image_url(entity.getImage_url())
                .build();
    }

    public static Image toEntity(ImageDTO dto) {
        if (dto == null) return null;
        Image entity = new Image();
        entity.setId(dto.getId());
        entity.setImage_url(dto.getImage_url());
        return entity;
    }
}
