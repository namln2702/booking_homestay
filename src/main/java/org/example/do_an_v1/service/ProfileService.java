package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.AdminProfileUpdateRequest;
import org.example.do_an_v1.dto.request.CustomerProfileUpdateRequest;
import org.example.do_an_v1.dto.request.HostProfileUpdateRequest;
import org.example.do_an_v1.payload.ApiResponse;

public interface ProfileService {

    ApiResponse<CustomerDTO> updateCustomerProfile(Long userId, CustomerProfileUpdateRequest request);

    ApiResponse<HostDTO> updateHostProfile(Long userId, HostProfileUpdateRequest request);

    ApiResponse<AdminDTO> updateAdminProfile(Long userId, AdminProfileUpdateRequest request);
}
