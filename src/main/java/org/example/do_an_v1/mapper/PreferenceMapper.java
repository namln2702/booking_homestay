package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.PreferenceDTO;
import org.example.do_an_v1.entity.Preference;

public class PreferenceMapper {

    /**
     * Chuyển từ Entity Preference sang DTO
     */
    public static PreferenceDTO toDTO(Preference preference) {
        if (preference == null) {
            return null;
        }

        return PreferenceDTO.builder()
                .id(preference.getId())
                .name(preference.getName())
                .description(preference.getDescription())
                .build();
    }

    /**
     * Chuyển từ DTO sang Entity Preference (dùng khi tạo mới / cập nhật)
     */
    public static Preference toEntity(PreferenceDTO dto) {
        if (dto == null) {
            return null;
        }

        return Preference.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}

