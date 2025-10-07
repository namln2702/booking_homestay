package org.example.do_an_v1.controller;


import org.example.do_an_v1.dto.UserDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private EmailService emailService;
//    @PostMapping("/register")
//    ApiResponse<> register(@RequestBody UserDTO userDTO){
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    @PostMapping("")
//    ResponseEntity<?> changePassword(@RequestBody UserReq userReq){
//        return new ResponseEntity<>(HttpStatus.OK);
//    }


//    @GetMapping("/test-email")
//    ApiResponse<Boolean> sendEmail(){
//        return emailService.sendSimpleEmail("lainguyennam270203@gmail.com");
//    }
}
