package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hosts")
public class HostController {

    private final HostService hostService;
    private final RequestIdentityResolver identityResolver;

    /**
     * register a new host
     */
    @PostMapping
    public ApiResponse<HostDTO> upsertHost(@RequestBody @Valid HostDTO hostDTO) {
        Long effectiveUserId = identityResolver.requireUserId(hostDTO.getIdUser());
        hostDTO.setIdUser(effectiveUserId);
        return hostService.upsertHostProfile(hostDTO);
    }

    // Fetch the authenticated host profile
    @GetMapping("/me")
    public ApiResponse<HostDTO> getMyHostProfile() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return hostService.getHostByUserId(effectiveUserId);
    }

    // Admin-only: fetch host profile for a specific user id
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<HostDTO> getHostForAdmin(@PathVariable Long userId) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        return hostService.getHostByUserId(effectiveUserId);
    }
}
