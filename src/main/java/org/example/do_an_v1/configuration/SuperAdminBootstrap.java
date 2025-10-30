package org.example.do_an_v1.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.LevelAdmin;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Minimal bootstrapper that seeds a super admin if none exists.
 * Controlled via properties so it stays inert unless explicitly configured.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Value("${bootstrap.super-admin.email:}")
    private String bootstrapEmail;

    @Value("${bootstrap.super-admin.name:Super Admin}")
    private String bootstrapName;

    @Value("${bootstrap.super-admin.username:superadmin}")
    private String bootstrapUsername;

    @Value("${bootstrap.super-admin.phone:}")
    private String bootstrapPhone;

    @Value("${bootstrap.super-admin.avatar-url:}")
    private String bootstrapAvatarUrl;

    @Value("${bootstrap.super-admin.age:}")
    private Integer bootstrapAge;

    @Override
    @Transactional
    public void run(String... args) {
        if (bootstrapEmail == null || bootstrapEmail.isBlank()) {
            return;
        }

        if (adminRepository.existsByLevelAdmin(LevelAdmin.SUPER_ADMIN)) {
            return;
        }

        User user = userRepository.findByEmail(bootstrapEmail);
        if (user == null) {
            user = User.builder()
                    .email(bootstrapEmail)
                    .username(bootstrapUsername)
                    .name(bootstrapName)
                    .phone(bootstrapPhone)
                    .avatarUrl(bootstrapAvatarUrl)
                    .age(bootstrapAge)
                    .isOnline(Boolean.FALSE)
                    .build();
            user = userRepository.save(user);
            log.info("Bootstrap super admin user created with email {}", bootstrapEmail);
        } else {
            boolean updated = false;
            if (bootstrapName != null && !bootstrapName.isBlank() && !bootstrapName.equals(user.getName())) {
                user.setName(bootstrapName);
                updated = true;
            }
            if (bootstrapUsername != null && !bootstrapUsername.isBlank() && !bootstrapUsername.equals(user.getUsername())) {
                user.setUsername(bootstrapUsername);
                updated = true;
            }
            if (bootstrapPhone != null && !bootstrapPhone.isBlank() && !bootstrapPhone.equals(user.getPhone())) {
                user.setPhone(bootstrapPhone);
                updated = true;
            }
            if (bootstrapAvatarUrl != null && !bootstrapAvatarUrl.isBlank() && !bootstrapAvatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(bootstrapAvatarUrl);
                updated = true;
            }
            if (bootstrapAge != null && !bootstrapAge.equals(user.getAge())) {
                user.setAge(bootstrapAge);
                updated = true;
            }
            if (Boolean.TRUE.equals(user.getIsOnline())) {
                user.setIsOnline(Boolean.FALSE);
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
                log.info("Bootstrap super admin user details refreshed for {}", bootstrapEmail);
            }
        }

        Admin admin = adminRepository.findById(user.getId()).orElse(null);
        if (admin == null) {
            admin = Admin.builder()
                    .user(user)
                    .levelAdmin(LevelAdmin.SUPER_ADMIN)
                    .status(Status.ACTIVE)
                    .role(RoleUser.ADMIN)
                    .build();
            admin = adminRepository.save(admin);
            user.setAdmin(admin);
            userRepository.save(user);
            log.info("Bootstrap super admin privileges granted to {}", bootstrapEmail);
        } else if (user.getAdmin() == null || !Objects.equals(user.getAdmin().getId(), admin.getId())) {
            user.setAdmin(admin);
            userRepository.save(user);
            log.info("Bootstrap super admin linkage repaired for {}", bootstrapEmail);
        }
    }
}
