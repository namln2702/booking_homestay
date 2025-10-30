package org.example.do_an_v1.service.support;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.exception.ResourceNotFoundException;
import org.example.do_an_v1.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserRegistrationSupport {

    private final UserRepository userRepository;

    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public boolean applyUserAttributes(User user, UserRegistrationRequest request) {
        boolean hasChanges = false;

        if (request.getUsername() != null && !Objects.equals(request.getUsername(), user.getUsername())) {
            user.setUsername(request.getUsername());
            hasChanges = true;
        }

        if (request.getName() != null && !Objects.equals(request.getName(), user.getName())) {
            user.setName(request.getName());
            hasChanges = true;
        }

        if (request.getPhone() != null && !Objects.equals(request.getPhone(), user.getPhone())) {
            user.setPhone(request.getPhone());
            hasChanges = true;
        }

        if (request.getAge() != null && !Objects.equals(request.getAge(), user.getAge())) {
            user.setAge(request.getAge());
            hasChanges = true;
        }

        if (request.getAvatarUrl() != null && !Objects.equals(request.getAvatarUrl(), user.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
            hasChanges = true;
        }

        return hasChanges;
    }
}
