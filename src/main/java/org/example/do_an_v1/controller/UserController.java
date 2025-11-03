package org.example.do_an_v1.controller;


import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.configuration.SessionConfig;
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

    @Autowired
    private SessionConfig sessionConfig;


    @PostMapping("/log-reg/login-register-with-email")
    ApiResponse<CodeForEmail> loginRegister(@RequestParam String email){
        System.out.println(sessionConfig.httpSession().getAttribute("id"));
        return userService.loginRegWithEmail(email);
    }
//
    @PostMapping("/log-reg/confirm-email")
    ApiResponse<?> confirmEmail(@RequestBody CodeForEmail codeForEmail){
        return userService.confirmEmail(codeForEmail);
    }

    @PostMapping("/log-reg/login-register-with-google")
    ApiResponse<?> getTokenWithTokenGG(@RequestParam("code") String tokenGG){
        log.info(tokenGG);
        return userService.loginRegEmailWithGoogle(tokenGG);
    }

    @GetMapping("/logout")
    ApiResponse<?> logout(){
        return userService.logout();
    }


//    @GetMapping("/test-email")
//    ApiResponse<Boolean> sendEmail(){
//        return emailService.sendSimpleEmail("lainguyennam270203@gmail.com");
//    }
}
