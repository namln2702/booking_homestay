package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.payload.ApiResponse;

public interface CustomerService {


    ApiResponse<CustomerDTO> upsertCustomerProfile(CustomerDTO customerDTO) throws RuntimeException;
    ApiResponse<CustomerDTO> getCustomerByUserId(Long userId);

    ApiResponse<?> booking(BillDTO billDTO);
}
