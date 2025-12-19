package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.UserDTO;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHost;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    /**
     * Map từ UserDTO sang User Entity
     */
    public User userDTOMapUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return User.builder()
                .name(userDTO.getName())
                .age(userDTO.getAge())
                .phone(userDTO.getPhone())
                .email(userDTO.getEmail())
                .avatarUrl(userDTO.getAvatarUrl())
                .isOnline(userDTO.getIsOnline())
                .username(userDTO.getUsername())
                .googleId(userDTO.getGoogleId())
                .build();
    }

    /**
     * Map từ User Entity sang UserDTO (không có roles và status)
     */
    public UserDTO userMapUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .name(user.getName())
                .age(user.getAge())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .isOnline(user.getIsOnline())
                .username(user.getUsername())
                .googleId(user.getGoogleId())
                .password(null) // Không trả về password
                .build();
    }

    /**
     * Map từ User Entity sang UserDTO với roles và các status (admin, host, customer)
     */
    public UserDTO toUserDTO(User user, List<String> roles, Status statusAdmin, StatusHost statusHost, Status statusCustomer) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .username(user.getUsername())
                .password(null) // Không trả về password
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .age(user.getAge())
                .isOnline(user.getIsOnline())
                .avatarUrl(user.getAvatarUrl())
                .googleId(user.getGoogleId())
                .role(roles)
                .statusAdmin(statusAdmin)
                .statusHost(statusHost)
                .statusCustomer(statusCustomer)
                .build();
    }
}

