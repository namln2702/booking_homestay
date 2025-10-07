package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.dto.UserDTO;
import org.example.do_an_v1.payload.ApiResponse;



public interface EmailService {

    ApiResponse<Boolean> sendSimpleEmail(String email, String code);




}
