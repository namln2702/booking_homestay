package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.CheckinRequest;
import org.example.do_an_v1.dto.request.CheckoutRequest;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hosts")
public class HostController {

    private final HostService hostService;
    private final RequestIdentityResolver identityResolver;

    @PostMapping
    public ApiResponse<HostDTO> registerHost(@RequestBody @Valid HostRegistrationRequest request) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return hostService.registerHost(effectiveUserId, request);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<List<HostDTO>>> listHosts(
            @RequestParam(name = "status", required = false, defaultValue = "PENDING") StatusHost status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Long adminUserId = identityResolver.requireUserId(null);
        return hostService.getHostsForAdmin(adminUserId, status, page, size);
    }

    @GetMapping("/me")
    public ApiResponse<HostDTO> getMyHostProfile() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return hostService.getHostByUserId(effectiveUserId);
    }

    /**
     * Thống kê danh sách homestay thuộc về host hiện tại
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/me/homestays")
    public ApiResponse<?> getMyHomestays() {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.getHomestaysForHost(hostUserId);
    }

    /**
     * Liệt kê các bill đã đặt (thành công/đang sử dụng) cho các homestay của host hiện tại
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/me/bills")
    public ApiResponse<?> getMyHomestayBills() {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.getBillsForHostHomestays(hostUserId);
    }

    // Admin-only: fetch host profile for a specific user id




    /**
     * Xác nhận check-in thành công
     * Yêu cầu quyền HOST
     * Host chỉ có thể check-in cho các bill thuộc về homestay của mình
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/checkin")
    public ApiResponse<?> confirmCheckin(@RequestBody @Valid CheckinRequest request) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.confirmCheckin(hostUserId, request);
    }

    /**
     * Xác nhận trả phòng thành công
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/checkout")
    public ApiResponse<?> confirmCheckout(@RequestBody @Valid CheckoutRequest request) {
        return hostService.confirmCheckout(request);
    }


    //  Cap nhap lai token host
    // @PreAuthorize("hasAuthority('ROLE_HOST')")
    // @PostMapping("/me/refresh-token")
    // public ApiResponse<String> refreshTokenForHost() {
    //     Long effectiveUserId = identityResolver.requireUserId(null);
    //     return hostService.refreshTokenForHost(effectiveUserId);
    // }

//    @PreAuthorize("hasAuthority('ROLE_HOST')")
//    @PostMapping("/me/homestays")
//    public ApiResponse<HomestayDTO> createHomestayForCurrentHost(
//            @RequestBody @Valid HomestayCreateRequest request
//    ) {
//        Long effectiveUserId = identityResolver.requireUserId(null);
//        System.out.println("HostController.createHomestayForCurrentHost: " + effectiveUserId );
//        return homestayService.createHomestay(effectiveUserId, request);
//    }
}
