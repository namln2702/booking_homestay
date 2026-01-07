package org.example.do_an_v1.service;

import org.example.do_an_v1.payload.ApiResponse;



public interface EmailService {

    ApiResponse<Boolean> sendSimpleEmail(String email, String code);

    ApiResponse<Boolean> sendComplaintStatusEmail(String customerEmail, String billCode, String oldStatus, String newStatus, String decisionBy);




}
