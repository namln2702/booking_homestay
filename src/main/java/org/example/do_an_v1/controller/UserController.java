package org.example.do_an_v1.controller;


import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;


    @PostMapping("/login-register-with-email")
    ApiResponse<CodeForEmail> loginRegister(@RequestParam String email){
        return userService.loginRegWithEmail(email);
    }
//
    @PostMapping("/confirm-email")
    ApiResponse<?> confirmEmail(@RequestBody CodeForEmail codeForEmail){
        return userService.confirmEmail(codeForEmail);
    }

    @PostMapping("/login-register-with-google")
    ApiResponse<?> getTokenWithTokenGG(@RequestParam("code") String tokenGG){
        log.info(tokenGG);
        return userService.loginRegEmailWithGoogle(tokenGG);
    }


//    @GetMapping("/test-email")
//    ApiResponse<Boolean> sendEmail(){
//        return emailService.sendSimpleEmail("lainguyennam270203@gmail.com");
//    }
}
