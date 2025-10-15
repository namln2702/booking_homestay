package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.dto.LogoutRequest;
import org.example.do_an_v1.payload.ApiResponse;

public interface UserService {


    // Register with username, password, email
    ApiResponse<CodeForEmail> loginRegWithEmail(String email) throws RuntimeException;

    ApiResponse<?> confirmEmail(CodeForEmail codeForEmail) throws RuntimeException;


    ApiResponse<?> logout() throws RuntimeException;

    // Register with google
    ApiResponse<?> loginRegEmailWithGoogle(String tokenGG) throws RuntimeException;

}
