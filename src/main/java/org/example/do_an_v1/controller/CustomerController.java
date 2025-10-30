package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.CustomerService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final RequestIdentityResolver identityResolver;

    /**
     * Persist or update customer-specific details using the unified DTO contract.
     * Aligns with future JWT adoption since the same DTO is returned.
     */
    @PostMapping
    public ApiResponse<CustomerDTO> upsertCustomer(@RequestBody @Valid CustomerDTO customerDTO) {
        Long effectiveUserId = identityResolver.requireUserId(customerDTO.getIdUser());
        customerDTO.setIdUser(effectiveUserId);
        return customerService.upsertCustomerProfile(customerDTO);
    }

    // Fetch the customer profile associated with the provided user identifier
    @GetMapping("/{userId}")
    public ApiResponse<CustomerDTO> getCustomer(@PathVariable Long userId) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        return customerService.getCustomerByUserId(effectiveUserId);
    }


}
