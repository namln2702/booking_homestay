package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.request.CustomerRegistrationRequest;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.CustomerRepository;
import org.example.do_an_v1.service.CustomerService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ProfileMapper profileMapper;
    private final UserRegistrationSupport userRegistrationSupport;

    @Override
    @Transactional
    public ApiResponse<CustomerDTO> registerCustomer(CustomerRegistrationRequest request) throws RuntimeException {
        User user = userRegistrationSupport.getUserOrThrow(request.getUserId());

        Customer customer = customerRepository.findById(user.getId()).orElse(null);
        boolean isNew = false;

        if (customer == null) {
            customer = Customer.builder()
                    .user(user)
                    .role(RoleUser.CUSTOMER)
                    .build();
            isNew = true;
        }

        boolean hasChanges = isNew;

        if (userRegistrationSupport.applyUserAttributes(user, request)) {
            hasChanges = true;
        }

        if (request.getStatus() != null && !Objects.equals(request.getStatus(), customer.getStatus())) {
            customer.setStatus(request.getStatus());
            hasChanges = true;
        }

        if (request.getDateOfBirth() != null && !Objects.equals(request.getDateOfBirth(), customer.getDateOfBirth())) {
            customer.setDateOfBirth(request.getDateOfBirth());
            hasChanges = true;
        }

        if (request.getQrCodeUrl() != null && !Objects.equals(request.getQrCodeUrl(), customer.getQrCodeUrl())) {
            customer.setQrCodeUrl(request.getQrCodeUrl());
            hasChanges = true;
        }

        // lastBooking will be maintained by booking workflows; ignore incoming value for now

        if (customer.getRole() != RoleUser.CUSTOMER) {
            customer.setRole(RoleUser.CUSTOMER);
            hasChanges = true;
        }

        if (!hasChanges) {
            return new ApiResponse<>(200, "Customer information already up to date", profileMapper.toCustomerDTO(customer));
        }

        // createdAt/updatedAt live on BaseEntity and are populated through auditing, never via the request payload
        Customer savedCustomer = customerRepository.save(customer);
        String message = isNew ? "Customer registered successfully" : "Customer information updated successfully";

        return new ApiResponse<>(200, message, profileMapper.toCustomerDTO(savedCustomer));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerDTO> getCustomerByUserId(Long userId) {
        Customer customer = customerRepository.findById(userId).orElse(null);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found", null);
        }
        return new ApiResponse<>(200, "Customer profile retrieved successfully", profileMapper.toCustomerDTO(customer));
    }
}
