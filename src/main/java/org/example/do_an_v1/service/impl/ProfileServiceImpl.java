package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.AdminProfileUpdateRequest;
import org.example.do_an_v1.dto.request.CustomerProfileUpdateRequest;
import org.example.do_an_v1.dto.request.HostProfileUpdateRequest;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.exception.ResourceNotFoundException;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.CustomerRepository;
import org.example.do_an_v1.repository.HostRepository;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final CustomerRepository customerRepository;
    private final HostRepository hostRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ApiResponse<CustomerDTO> updateCustomerProfile(Long userId, CustomerProfileUpdateRequest request) {
        if (request == null) {
            return new ApiResponse<>(400, "Customer profile payload is required", null);
        }

        Customer customer = customerRepository.findById(userId).orElse(null);
        boolean isNew = false;

        if (customer == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            customer = Customer.builder()
                    .user(user)
                    .role(RoleUser.CUSTOMER)
                    .build();
            isNew = true;
        }

        User user = customer.getUser();
        boolean hasChanges = isNew;

        hasChanges |= applyMutableUserFields(user,
                null,
                request.getName(),
                request.getPhone(),
                request.getAge(),
                request.getAvatarUrl());

        if (request.getDateOfBirth() != null && !Objects.equals(request.getDateOfBirth(), customer.getDateOfBirth())) {
            customer.setDateOfBirth(request.getDateOfBirth());
            hasChanges = true;
        }

        if (request.getQrCodeUrl() != null && !Objects.equals(request.getQrCodeUrl(), customer.getQrCodeUrl())) {
            customer.setQrCodeUrl(request.getQrCodeUrl());
            hasChanges = true;
        }

        // lastBooking is derived from booking workflows and should not be overridden by profile updates

        if (customer.getRole() != RoleUser.CUSTOMER) {
            customer.setRole(RoleUser.CUSTOMER);
            hasChanges = true;
        }

        if (!hasChanges) {
            return new ApiResponse<>(200, "No changes detected for customer profile", profileMapper.toCustomerDTO(customer));
        }

        Customer savedCustomer = customerRepository.save(customer);
        String message = isNew ? "Customer profile created successfully" : "Customer profile updated successfully";
        return new ApiResponse<>(200, message, profileMapper.toCustomerDTO(savedCustomer));
    }

    @Override
    @Transactional
    public ApiResponse<HostDTO> updateHostProfile(Long userId, HostProfileUpdateRequest request) {
        if (request == null) {
            return new ApiResponse<>(400, "Host profile payload is required", null);
        }

        Host host = hostRepository.findById(userId).orElse(null);
        boolean isNew = false;

        if (host == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            host = Host.builder()
                    .user(user)
                    .role(RoleUser.HOST)
                    .build();
            isNew = true;
        }

        User user = host.getUser();
        boolean hasChanges = isNew;

        hasChanges |= applyMutableUserFields(user,
                null,
                request.getName(),
                request.getPhone(),
                request.getAge(),
                request.getAvatarUrl());

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
            return new ApiResponse<>(200, "No changes detected for host profile", profileMapper.toHostDTO(host));
        }

        Host savedHost = hostRepository.save(host);
        String message = isNew ? "Host profile created successfully" : "Host profile updated successfully";
        return new ApiResponse<>(200, message, profileMapper.toHostDTO(savedHost));
    }

    @Override
    @Transactional
    public ApiResponse<AdminDTO> updateAdminProfile(Long userId, AdminProfileUpdateRequest request) {
        if (request == null) {
            return new ApiResponse<>(400, "Admin profile payload is required", null);
        }

        Admin admin = adminRepository.findById(userId).orElse(null);
        boolean isNew = false;

        if (admin == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            admin = Admin.builder()
                    .user(user)
                    .role(RoleUser.ADMIN)
                    .build();
            isNew = true;
        }

        User user = admin.getUser();
        boolean hasChanges = isNew;

        hasChanges |= applyMutableUserFields(user,
                null,
                request.getName(),
                request.getPhone(),
                request.getAge(),
                request.getAvatarUrl());

        if (request.getLevelAdmin() != null && !Objects.equals(request.getLevelAdmin(), admin.getLevelAdmin())) {
            admin.setLevelAdmin(request.getLevelAdmin());
            hasChanges = true;
        }

        if (request.getStatus() != null && !Objects.equals(request.getStatus(), admin.getStatus())) {
            admin.setStatus(request.getStatus());
            hasChanges = true;
        }

        if (admin.getRole() != RoleUser.ADMIN) {
            admin.setRole(RoleUser.ADMIN);
            hasChanges = true;
        }

        if (!hasChanges) {
            return new ApiResponse<>(200, "No changes detected for admin profile", profileMapper.toAdminDTO(admin));
        }

        Admin savedAdmin = adminRepository.save(admin);
        String message = isNew ? "Admin profile created successfully" : "Admin profile updated successfully";
        return new ApiResponse<>(200, message, profileMapper.toAdminDTO(savedAdmin));
    }

    private boolean applyMutableUserFields(User user,
                                           String username,
                                           String name,
                                           String phone,
                                           Integer age,
                                           String avatarUrl) {
        boolean changed = false;

        if (username != null && !Objects.equals(username, user.getUsername())) {
            user.setUsername(username);
            changed = true;
        }

        if (name != null && !Objects.equals(name, user.getName())) {
            user.setName(name);
            changed = true;
        }

        if (phone != null && !Objects.equals(phone, user.getPhone())) {
            user.setPhone(phone);
            changed = true;
        }

        if (age != null && !Objects.equals(age, user.getAge())) {
            user.setAge(age);
            changed = true;
        }

        if (avatarUrl != null && !Objects.equals(avatarUrl, user.getAvatarUrl())) {
            user.setAvatarUrl(avatarUrl);
            changed = true;
        }

        return changed;
    }

}
