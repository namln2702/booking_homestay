package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.CustomerTierDTO;
import org.example.do_an_v1.payload.ApiResponse;

public interface CustomerTierService {
    ApiResponse<CustomerTierDTO> getCustomerTier(Long customerId);
}
