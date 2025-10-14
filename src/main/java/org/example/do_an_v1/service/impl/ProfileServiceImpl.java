package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.AdminProfileUpdateRequest;
import org.example.do_an_v1.dto.request.CustomerProfileUpdateRequest;
import org.example.do_an_v1.dto.request.HostProfileUpdateRequest;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.User;
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
        Customer customer = customerRepository.findById(userId).orElse(null);
        boolean isNew = false;

        if (customer == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            customer = Customer.builder().user(user).build();
            isNew = true;
        }

        User user = customer.getUser();

        boolean hasChanges = false;

        if (request.getName() != null && !Objects.equals(request.getName(), user.getName())) {
            user.setName(request.getName());
            hasChanges = true;
        }

        if (request.getPhone() != null && !Objects.equals(request.getPhone(), user.getPhone())) {
            user.setPhone(request.getPhone());
            hasChanges = true;
        }

        if (request.getAge() != null && !Objects.equals(request.getAge(), user.getAge())) {
            user.setAge(request.getAge());
            hasChanges = true;
        }

        if (request.getAvatarUrl() != null && !Objects.equals(request.getAvatarUrl(), user.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
            hasChanges = true;
        }

        if (request.getDateOfBirth() != null && !Objects.equals(request.getDateOfBirth(), customer.getDateOfBirth())) {
            customer.setDateOfBirth(request.getDateOfBirth());
            hasChanges = true;
        }

        if (hasChanges || isNew) {
            customerRepository.save(customer);
            return new ApiResponse<>(200, "Customer profile updated successfully", profileMapper.toCustomerDTO(customer));
        }

        return new ApiResponse<>(200, "No changes detected for customer profile", profileMapper.toCustomerDTO(customer));
    }

    @Override
    @Transactional
    public ApiResponse<HostDTO> updateHostProfile(Long userId, HostProfileUpdateRequest request) {
        Host host = hostRepository.findById(userId).orElse(null);
        boolean isNew = false;

        if (host == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            host = Host.builder().user(user).build();
            isNew = true;
        }

        User user = host.getUser();

        boolean hasChanges = false;

        if (request.getName() != null && !Objects.equals(request.getName(), user.getName())) {
            user.setName(request.getName());
            hasChanges = true;
        }

        if (request.getPhone() != null && !Objects.equals(request.getPhone(), user.getPhone())) {
            user.setPhone(request.getPhone());
            hasChanges = true;
        }

        if (request.getAge() != null && !Objects.equals(request.getAge(), user.getAge())) {
            user.setAge(request.getAge());
            hasChanges = true;
        }

        if (request.getAvatarUrl() != null && !Objects.equals(request.getAvatarUrl(), user.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
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

        if (hasChanges || isNew) {
            hostRepository.save(host);
            return new ApiResponse<>(200, "Host profile updated successfully", profileMapper.toHostDTO(host));
        }

        return new ApiResponse<>(200, "No changes detected for host profile", profileMapper.toHostDTO(host));
    }

    @Override
    @Transactional
    public ApiResponse<AdminDTO> updateAdminProfile(Long userId, AdminProfileUpdateRequest request) {
        Admin admin = adminRepository.findById(userId).orElse(null);
        boolean isNew = false;

        if (admin == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            admin = Admin.builder().user(user).build();
            isNew = true;
        }

        User user = admin.getUser();

        boolean hasChanges = false;

        if (request.getName() != null && !Objects.equals(request.getName(), user.getName())) {
            user.setName(request.getName());
            hasChanges = true;
        }

        if (request.getPhone() != null && !Objects.equals(request.getPhone(), user.getPhone())) {
            user.setPhone(request.getPhone());
            hasChanges = true;
        }

        if (request.getAge() != null && !Objects.equals(request.getAge(), user.getAge())) {
            user.setAge(request.getAge());
            hasChanges = true;
        }

        if (request.getAvatarUrl() != null && !Objects.equals(request.getAvatarUrl(), user.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
            hasChanges = true;
        }

        if (request.getLevelAdmin() != null && !Objects.equals(request.getLevelAdmin(), admin.getLevelAdmin())) {
            admin.setLevelAdmin(request.getLevelAdmin());
            hasChanges = true;
        }

        if (request.getStatus() != null && !Objects.equals(request.getStatus(), admin.getStatus())) {
            admin.setStatus(request.getStatus());
            hasChanges = true;
        }

        if (hasChanges || isNew) {
            adminRepository.save(admin);
            return new ApiResponse<>(200, "Admin profile updated successfully", profileMapper.toAdminDTO(admin));
        }

        return new ApiResponse<>(200, "No changes detected for admin profile", profileMapper.toAdminDTO(admin));
    }
}
