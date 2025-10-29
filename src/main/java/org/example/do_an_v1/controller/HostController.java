package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hosts")
public class HostController {

    private final HostService hostService;

    // Persist host-specific details after the base user has been created
    @PostMapping("/register")
    public ApiResponse<HostDTO> registerHost(@RequestBody @Valid HostRegistrationRequest request) {
        return hostService.registerHost(request);
    }

    // Fetch the host profile associated with the provided user identifier
    @GetMapping("/{userId}")
    public ApiResponse<HostDTO> getHost(@PathVariable Long userId) {
        return hostService.getHostByUserId(userId);
    }
}
