package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hosts")
public class HostController {

    private final HostService hostService;
    private final RequestIdentityResolver identityResolver;

    /**
     * Persist or update host-specific details using the unified DTO contract.
     */
    @PostMapping
    public ApiResponse<HostDTO> upsertHost(@RequestBody @Valid HostDTO hostDTO) {
        Long effectiveUserId = identityResolver.requireUserId(hostDTO.getIdUser());
        hostDTO.setIdUser(effectiveUserId);
        return hostService.upsertHostProfile(hostDTO);
    }

    // Fetch the host profile associated with the provided user identifier
    @GetMapping("/{userId}")
    public ApiResponse<HostDTO> getHost(@PathVariable Long userId) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        return hostService.getHostByUserId(effectiveUserId);
    }
}
