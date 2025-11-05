package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface HostService {

    ApiResponse<HostDTO> registerHost(Long userId, HostRegistrationRequest request) throws RuntimeException;

    ApiResponse<PageResponse<List<HostDTO>>> getHostsForAdmin(Long adminUserId, StatusHost status, int page, int size);

    ApiResponse<HostDTO> getHostDetailForAdmin(Long adminUserId, Long hostUserId);

    ApiResponse<HostDTO> approveHost(Long adminUserId, Long hostUserId);

    ApiResponse<HostDTO> getHostByUserId(Long userId);
}
