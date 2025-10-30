package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
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
    public ApiResponse<HostDTO> upsertHostProfile(HostDTO hostDTO) throws RuntimeException {
        if (hostDTO == null || hostDTO.getIdUser() == null) {
            throw new IllegalArgumentException("Host DTO must include idUser");
        }

        User user = userRegistrationSupport.getUserOrThrow(hostDTO.getIdUser());

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

        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                hostDTO.getIdUser(),
                hostDTO.getUsername(),
                hostDTO.getName(),
                hostDTO.getPhone(),
                hostDTO.getAge(),
                hostDTO.getAvatarUrl()
        );

        if (userRegistrationSupport.applyUserAttributes(user, userRequest)) {
            hasChanges = true;
        }

        if (hostDTO.getStatusHost() != null && !Objects.equals(hostDTO.getStatusHost(), host.getStatusHost())) {
            host.setStatusHost(hostDTO.getStatusHost());
            hasChanges = true;
        }

        if (hostDTO.getBusinessName() != null && !Objects.equals(hostDTO.getBusinessName(), host.getBusinessName())) {
            host.setBusinessName(hostDTO.getBusinessName());
            hasChanges = true;
        }

        if (hostDTO.getQrCodeUrl() != null && !Objects.equals(hostDTO.getQrCodeUrl(), host.getQrCodeUrl())) {
            host.setQrCodeUrl(hostDTO.getQrCodeUrl());
            hasChanges = true;
        }

        if (host.getRole() != RoleUser.HOST) {
            host.setRole(RoleUser.HOST);
            hasChanges = true;
        }

        if (!hasChanges) {
            return new ApiResponse<>(200, "Host information already up to date", profileMapper.toHostDTO(host));
        }

        // BaseEntity auditing handles createdAt/updatedAt so these timestamps never come from client input
        Host savedHost = hostRepository.save(host);
        String message = isNew ? "Host profile created successfully" : "Host information updated successfully";

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
