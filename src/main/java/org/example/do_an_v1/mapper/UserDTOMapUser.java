package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.UserDTO;
import org.example.do_an_v1.entity.Users;

public class UserDTOMapUser {
    public Users userDTOMapUser(UserDTO userDTO){
        return Users.builder()
                .name(userDTO.getName())
                .age(userDTO.getAge())
                .phone(userDTO.getPhone())
                .email(userDTO.getEmail())
                .avatarUrl(userDTO.getAvatarUrl())
                .isOnline(userDTO.getIsOnline())
                .roleUser(userDTO.getRoleUser())
                .password(userDTO.getPassword())
                .username(userDTO.getUsername())
                .googleId(userDTO.getGoogleId())
                .build();
    }
}
