package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.UserDTO;
import org.example.do_an_v1.entity.User;
import org.springframework.stereotype.Component;


@Component
public class UserDTOMapUser {
    public User userDTOMapUser(UserDTO userDTO){
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

    public UserDTO userMapUserDTO(User user){

        return UserDTO.builder()
                .name(user.getName())
                .age(user.getAge())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .isOnline(user.getIsOnline())
                .username(user.getUsername())
                .googleId(user.getGoogleId())
                .build();

    }

}
