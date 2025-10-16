package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {

    private final UserService userService;

    @Override
    public ApiResponse<CodeForEmail> createVerifyCode(String email) throws RuntimeException {
        return userService.loginRegWithEmail(email);
    }

    @Override
    public ApiResponse<?> confirmEmail(CodeForEmail codeForEmail) throws RuntimeException {
        return userService.confirmEmail(codeForEmail);
    }

    @Override
    public ApiResponse<?> registerEmailWithGoogle(String tokenGG) throws RuntimeException {
        return userService.loginRegEmailWithGoogle(tokenGG);
    }
}

