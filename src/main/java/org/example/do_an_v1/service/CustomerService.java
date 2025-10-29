package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.request.CustomerRegistrationRequest;
import org.example.do_an_v1.payload.ApiResponse;

public interface CustomerService {


    ApiResponse<CustomerDTO> registerCustomer(CustomerRegistrationRequest request) throws RuntimeException;

    ApiResponse<CustomerDTO> getCustomerByUserId(Long userId);
}
