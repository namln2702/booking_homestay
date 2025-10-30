package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.AdminService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;
    private final RequestIdentityResolver identityResolver;

    // Only existing admins may issue new invitations
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PostMapping("/invite")
    public ApiResponse<AdminInvitationResponse> inviteAdmin(@RequestBody @Valid AdminInviteRequest request) {
        Long actorId = identityResolver.requireUserId(null);
        return adminService.inviteAdmin(actorId, request);
    }


    // Invited users complete activation without requiring prior authentication
    @PostMapping("/activate")
    public ApiResponse<AdminDTO> activateAdmin(@RequestBody @Valid AdminActivationRequest request) {
        return adminService.activateAdmin(request);
    }
}
