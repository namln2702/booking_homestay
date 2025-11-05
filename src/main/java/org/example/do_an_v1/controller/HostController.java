package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.HomestayService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hosts")
public class HostController {

    private final HostService hostService;
    private final HomestayService homestayService;
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

    // Admin-only: fetch host profile for a specific user id
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<HostDTO> getHostForAdmin(@PathVariable Long userId) {
        Long adminUserId = identityResolver.requireUserId(null);
        return hostService.getHostDetailForAdmin(adminUserId, userId);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/{userId}/approve")
    public ApiResponse<HostDTO> approveHost(@PathVariable Long userId) {
        Long adminUserId = identityResolver.requireUserId(null);
        return hostService.approveHost(adminUserId, userId);
    }

    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/me/homestays")
    public ApiResponse<HomestayDTO> createHomestayForCurrentHost(
            @RequestBody @Valid HomestayCreateRequest request
    ) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        System.out.println("HostController.createHomestayForCurrentHost: " + effectiveUserId );
        return homestayService.createHomestay(effectiveUserId, request);
    }
}
