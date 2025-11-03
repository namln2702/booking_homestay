package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.payload.ApiResponse;

public interface HostService {

    ApiResponse<HostDTO> registerHost(Long userId, HostRegistrationRequest request) throws RuntimeException;

    ApiResponse<HostDTO> approveHost(Long adminUserId, Long hostUserId);

    ApiResponse<HostDTO> getHostByUserId(Long userId);
}
