package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.AdminService;
import org.example.do_an_v1.service.HomestayService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;
    private final HomestayService homestayService;
    private final RequestIdentityResolver identityResolver;

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @GetMapping("/homestays")
    public ApiResponse<PageResponse<List<HomestaySummaryDTO>>> listHomestays(
            @RequestParam(name = "status", required = false, defaultValue = "PENDING") StatusHomestay status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Long actorId = identityResolver.requireUserId(null);
        return homestayService.getHomestaysForAdmin(actorId, status, page, size);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @GetMapping("/homestays/{homestayId}")
    public ApiResponse<HomestayDTO> getHomestayDetail(@PathVariable Long homestayId) {
        Long actorId = identityResolver.requireUserId(null);
        return homestayService.getHomestayDetailForAdmin(actorId, homestayId);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PostMapping("/invite")
    public ApiResponse<AdminInvitationResponse> inviteAdmin(@RequestBody @Valid AdminInviteRequest request) {
        Long actorId = identityResolver.requireUserId(null);
        return adminService.inviteAdmin(actorId, request);
    }

    @PostMapping("/activate")
    public ApiResponse<AdminDTO> activateAdmin(@RequestBody @Valid AdminActivationRequest request) {
        return adminService.activateAdmin(request);
    }

    // Admins approve homestays submitted by hosts
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PutMapping("/homestays/{homestayId}/approve")
    public ApiResponse<HomestayDTO> approveHomestay(@PathVariable Long homestayId) {
        Long actorId = identityResolver.requireUserId(null);
        return homestayService.approveHomestay(actorId, homestayId);
    }
}
