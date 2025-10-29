package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.AdminService;
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

    // Issue a new admin invitation email and return the invitation payload
    @PostMapping("/invite")
    public ApiResponse<AdminInvitationResponse> inviteAdmin(@RequestBody @Valid AdminInviteRequest request) {
        return adminService.inviteAdmin(request);
    }


    @PreAuthorize("ADMIN")
    // Activate an invited admin account once they provide the confirmation details
    @PostMapping("/activate")
    public ApiResponse<AdminDTO> activateAdmin(@RequestBody @Valid AdminActivationRequest request) {
        return adminService.activateAdmin(request);
    }
}
