package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.dto.UserDTO;
import org.example.do_an_v1.dto.UserRes;
import org.example.do_an_v1.payload.ApiResponse;

public interface UserService {

    ApiResponse<CodeForEmail> createVerifyCode(UserDTO userDTO) throws RuntimeException;

    ApiResponse<?> confirmEmail(CodeForEmail codeForEmail) throws RuntimeException;
}
