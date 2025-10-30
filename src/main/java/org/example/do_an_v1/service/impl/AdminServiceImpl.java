package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.entity.ConfirmEmail;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.LevelAdmin;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.exception.ResourceNotFoundException;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.ConfirmEmailRepository;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.AdminService;
import org.example.do_an_v1.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final int INVITE_CODE_LENGTH = 6;

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ConfirmEmailRepository confirmEmailRepository;
    private final EmailService emailService;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ApiResponse<AdminInvitationResponse> inviteAdmin(Long actorAdminId, AdminInviteRequest request) throws RuntimeException {
        Admin actingAdmin = adminRepository.findById(actorAdminId).orElse(null);
        if (Objects.isNull(actingAdmin) || actingAdmin.getLevelAdmin() != LevelAdmin.SUPER_ADMIN) {
            return new ApiResponse<>(403, "Only super admins may invite new admins", null);
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail);
        if (Objects.isNull(user)) {
            user = User.builder()
                    .email(normalizedEmail)
                    .name(request.getFullName())
                    .phone(request.getPhone())
                    .build();
        } else if (Objects.nonNull(user.getAdmin()) && Status.ACTIVE.equals(user.getAdmin().getStatus())) {
            Admin existingAdmin = user.getAdmin();
            AdminInvitationResponse conflictResponse = AdminInvitationResponse.builder()
                    .admin(profileMapper.toAdminDTO(existingAdmin))
                    .activationCode(null)
                    .build();
            return new ApiResponse<>(409,
                    "Admin account already active for email: " + normalizedEmail,
                    conflictResponse);
        } else {
            user.setName(request.getFullName());
            if (Objects.nonNull(request.getPhone())) {
                user.setPhone(request.getPhone());
            }
        }

        user = userRepository.save(user);

        Admin admin = adminRepository.findById(user.getId()).orElse(null);
        if (Objects.isNull(admin)) {
            admin = Admin.builder()
                    .user(user)
                    .status(Status.INACTIVE)
                    .levelAdmin(Objects.requireNonNullElse(request.getLevelAdmin(), LevelAdmin.ADMIN))
                    .build();
        } else {
            admin.setStatus(Status.INACTIVE);
            admin.setLevelAdmin(Objects.requireNonNullElse(request.getLevelAdmin(), admin.getLevelAdmin()));
        }
        admin.setRole(RoleUser.ADMIN);
        // BaseEntity timestamps (createdAt/updatedAt) auto-populate here; requests never control them
        admin = adminRepository.save(admin);

        String inviteCode = generateInviteCode();
        // expired_at is derived here so invitation validity stays under server control, never taken from client input
        confirmEmailRepository.save(ConfirmEmail.builder()
                .email(normalizedEmail)
                .code(inviteCode)
                .expired_at(LocalDateTime.now().plusHours(24))
                .build());

        emailService.sendSimpleEmail(normalizedEmail, buildInvitationMessage(inviteCode));

        AdminInvitationResponse response = AdminInvitationResponse.builder()
                .admin(profileMapper.toAdminDTO(admin))
                .activationCode(inviteCode)
                .build();

        return new ApiResponse<>(200,
                "Invitation sent successfully. Activation code is returned for testing purposes only.",
                response);
    }

    @Override
    @Transactional
    public ApiResponse<AdminDTO> activateAdmin(AdminActivationRequest request) throws RuntimeException {
        ConfirmEmail confirmEmail = confirmEmailRepository.findByEmailAndCode(request.getEmail(), request.getCode());

        if (Objects.isNull(confirmEmail)) {
            return new ApiResponse<>(422, "Invalid activation code", null);
        }

        if (LocalDateTime.now().isAfter(confirmEmail.getExpired_at())) {
            return new ApiResponse<>(422, "Activation code expired", null);
        }

        User user = userRepository.findByEmail(request.getEmail());
        if (Objects.isNull(user)) {
            throw new ResourceNotFoundException("User not found with email: " + request.getEmail());
        }

        Admin admin = adminRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin account not found for user id: " + user.getId()));

        if (Objects.nonNull(request.getFullName())) {
            user.setName(request.getFullName());
        }
        if (Objects.nonNull(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);

        admin.setStatus(Status.ACTIVE);
        admin = adminRepository.save(admin);

        return new ApiResponse<>(200, "Admin account activated successfully", profileMapper.toAdminDTO(admin));
    }

    private String generateInviteCode() {
        SecureRandom secureRandom = new SecureRandom();
        return IntStream.range(0, INVITE_CODE_LENGTH)
                .map(i -> secureRandom.nextInt(10))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    private String buildInvitationMessage(String code) {
        return "You have been invited to join the admin team. Use this one-time activation code within 24 hours: " + code;
    }
}
