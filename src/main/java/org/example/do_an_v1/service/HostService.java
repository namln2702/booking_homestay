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

    /**
     * Thống kê danh sách homestay theo host (dùng userId của host)
     */
    ApiResponse<?> getHomestaysForHost(Long hostUserId);

    /**
     * Liệt kê các bill đã đặt (thành công) của các homestay thuộc host
     */
    ApiResponse<?> getBillsForHostHomestays(Long hostUserId);

    ApiResponse<?> confirmCheckin(Long hostUserId, org.example.do_an_v1.dto.request.CheckinRequest request);

    ApiResponse<?> confirmCheckout(org.example.do_an_v1.dto.request.CheckoutRequest request);
}
