package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.User;
import org.springframework.stereotype.Component;


@Component
public class AdminMapper {

    /**
     * Map từ Admin Entity sang AdminDTO
     */
    public static AdminDTO adminMapAdminDTO(Admin admin) {
        if (admin == null) return null;

        User user = admin.getUser();

        return AdminDTO.builder()
                .idAdmin(admin.getId())
                .idUser(admin.getUser().getId())
                .levelAdmin(admin.getLevelAdmin())
                .status(admin.getStatus())
                .role(admin.getRole())
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .isOnline(user != null ? user.getIsOnline() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .age(user != null ? user.getAge() : null)
                .name(user != null ? user.getName() : null)
                .googleId(user != null ? user.getGoogleId() : null)
                .build();
    }

    /**
     * Map từ AdminDTO sang Admin Entity
     */
//    public static Admin adminDTOMapAdmin(AdminDTO dto) {
//        if (dto == null) return null;
//
//        // Tạo User entity từ DTO
//        User user = User.builder()
//                .username(dto.getUsername())
//                .email(dto.getEmail())
//                .phone(dto.getPhone())
//                .isOnline(dto.getIsOnline())
//                .avatarUrl(dto.getAvatarUrl())
//                .age(dto.getAge())
//                .name(dto.getName())
//                .googleId(dto.getGoogleId())
//                .build();
//
//        // Tạo Admin entity
//        return Admin.builder()
//                .id(dto.getId())
//                .levelAdmin(dto.getLevelAdmin())
//                .status(dto.getStatus())
//                .role(dto.getRole())
//                .user(user)
//                .build();
//    }
}