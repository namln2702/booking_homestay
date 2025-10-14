package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hosts")
public class HostController {

    private final HostService hostService;

    @PostMapping("/login-register-with-email")
    public ApiResponse<CodeForEmail> registerWithEmail(@RequestParam String email) {
        return hostService.createVerifyCode(email);
    }

    @PostMapping("/confirm-email")
    public ApiResponse<?> confirmEmail(@RequestBody @Valid CodeForEmail codeForEmail) {
        return hostService.confirmEmail(codeForEmail);
    }

    @PostMapping("/login-register-with-google")
    public ApiResponse<?> registerWithGoogle(@RequestParam("code") String tokenGG) {
        return hostService.registerEmailWithGoogle(tokenGG);
    }
}

