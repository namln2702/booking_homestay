package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.CustomerService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final RequestIdentityResolver identityResolver;

    /**
     * register a new customer
     */
    @PostMapping
    public ApiResponse<CustomerDTO> upsertCustomer(@RequestBody @Valid CustomerDTO customerDTO) {
        Long effectiveUserId = identityResolver.requireUserId(customerDTO.getIdUser());
        customerDTO.setIdUser(effectiveUserId);
        return customerService.upsertCustomerProfile(customerDTO);
    }

    // Fetch the authenticated customer's profile
    @GetMapping("/me")
    public ApiResponse<CustomerDTO> getMyCustomerProfile() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.getCustomerByUserId(effectiveUserId);
    }

    // Admin-only: fetch the customer profile associated with the provided user identifier
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<CustomerDTO> getCustomerForAdmin(@PathVariable Long userId) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        return customerService.getCustomerByUserId(effectiveUserId);
    }

    @PostMapping("/booking")
    ApiResponse<?> booking(@RequestBody BillDTO billDTO){
        return customerService.booking(billDTO);
    }



}
