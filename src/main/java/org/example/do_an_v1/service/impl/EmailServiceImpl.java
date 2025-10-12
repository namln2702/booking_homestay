package org.example.do_an_v1.service.impl;

import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }
    @Override
    public ApiResponse<Boolean> sendSimpleEmail(String email, String code) {

        ApiResponse<Boolean> apiResponse = new ApiResponse<>(200, "Send success", true );
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Confirm email");
            message.setText(code);
            javaMailSender.send(message);
            return apiResponse;
        }catch (Exception e){

            apiResponse.setStatus(404);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setData(false);
            return apiResponse;
        }

    }



}
