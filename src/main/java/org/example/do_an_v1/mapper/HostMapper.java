package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.User;
import org.springframework.stereotype.Component;


@Component
public class HostMapper {

    /**
     * Map từ Host Entity sang HostDTO
     */
    public static HostDTO hostMapHostDTO(Host host) {
        if (host == null) return null;

        User user = host.getUser();

        return HostDTO.builder()
                .idHost(host.getId())
                .idUser(host.getUser().getId())
                .statusHost(host.getStatusHost())
                .businessName(host.getBusinessName())
                .qrCodeUrl(host.getQrCodeUrl())
                .role(host.getRole())
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
     * Map từ HostDTO sang Host Entity
     */
//    public static Host hostDTOMapHost(HostDTO dto) {
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
//        // Tạo Host entity
//        return Host.builder()
//                .id(dto.getId())
//                .statusHost(dto.getStatusHost())
//                .businessName(dto.getBusinessName())
//                .qrCodeUrl(dto.getQrCodeUrl())
//                .role(dto.getRole())
//                .user(user)
//                .build();
//    }
}