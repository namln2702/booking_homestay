package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.payload.ApiResponse;

public interface CustomerService {


    // Register with username, password, email
    ApiResponse<CodeForEmail> createVerifyCode(String email) throws RuntimeException;

    ApiResponse<?> confirmEmail(CodeForEmail codeForEmail) throws RuntimeException;



    // Register with google
    ApiResponse<?> registerEmailWithGoogle(String tokenGG) throws RuntimeException;

}
