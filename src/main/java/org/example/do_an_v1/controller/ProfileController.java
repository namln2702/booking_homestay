package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.ProfileService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final RequestIdentityResolver identityResolver;

    // Update or create the customer profile tied to the given user
    @PutMapping("/customers/{userId}")
    public ApiResponse<CustomerDTO> updateCustomerProfile(@PathVariable Long userId,
                                                          @RequestBody @Valid CustomerDTO request) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        if (request != null) {
            request.setIdUser(effectiveUserId);
        }
        return profileService.updateCustomerProfile(effectiveUserId, request);
    }

    // Update or create the host profile tied to the given user
    @PutMapping("/hosts/{userId}")
    public ApiResponse<HostDTO> updateHostProfile(@PathVariable Long userId,
                                                  @RequestBody @Valid HostDTO request) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        if (request != null) {
            request.setIdUser(effectiveUserId);
        }
        return profileService.updateHostProfile(effectiveUserId, request);
    }

    // Update or create the admin profile tied to the given user
    @PutMapping("/admins/{userId}")
    public ApiResponse<AdminDTO> updateAdminProfile(@PathVariable Long userId,
                                                    @RequestBody @Valid AdminDTO request) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        if (request != null) {
            request.setIdUser(effectiveUserId);
        }
        return profileService.updateAdminProfile(effectiveUserId, request);
    }
}
