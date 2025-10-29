package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.HostRepository;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {

    private final HostRepository hostRepository;
    private final ProfileMapper profileMapper;
    private final UserRegistrationSupport userRegistrationSupport;

    @Override
    @Transactional
    public ApiResponse<HostDTO> registerHost(HostRegistrationRequest request) throws RuntimeException {
        User user = userRegistrationSupport.getUserOrThrow(request.getUserId());

        Host host = hostRepository.findById(user.getId()).orElse(null);
        boolean isNew = false;

        if (host == null) {
            host = Host.builder()
                    .user(user)
                    .role(RoleUser.HOST)
                    .build();
            isNew = true;
        }

        boolean hasChanges = isNew;

        if (userRegistrationSupport.applyUserAttributes(user, request)) {
            hasChanges = true;
        }

        if (request.getStatusHost() != null && !Objects.equals(request.getStatusHost(), host.getStatusHost())) {
            host.setStatusHost(request.getStatusHost());
            hasChanges = true;
        }

        if (request.getBusinessName() != null && !Objects.equals(request.getBusinessName(), host.getBusinessName())) {
            host.setBusinessName(request.getBusinessName());
            hasChanges = true;
        }

        if (request.getQrCodeUrl() != null && !Objects.equals(request.getQrCodeUrl(), host.getQrCodeUrl())) {
            host.setQrCodeUrl(request.getQrCodeUrl());
            hasChanges = true;
        }

        if (host.getRole() != RoleUser.HOST) {
            host.setRole(RoleUser.HOST);
            hasChanges = true;
        }

        if (!hasChanges) {
            return new ApiResponse<>(200, "Host information already up to date", profileMapper.toHostDTO(host));
        }

        Host savedHost = hostRepository.save(host);
        String message = isNew ? "Host registered successfully" : "Host information updated successfully";

        return new ApiResponse<>(200, message, profileMapper.toHostDTO(savedHost));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HostDTO> getHostByUserId(Long userId) {
        Host host = hostRepository.findById(userId).orElse(null);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found", null);
        }
        return new ApiResponse<>(200, "Host profile retrieved successfully", profileMapper.toHostDTO(host));
    }
}
