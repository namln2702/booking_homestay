package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.request.CustomerRegistrationRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.CustomerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    // Persist customer-specific details after the base user has been created
    @PostMapping("/register")
    public ApiResponse<CustomerDTO> registerCustomer(@RequestBody @Valid CustomerRegistrationRequest request) {
        return customerService.registerCustomer(request);
    }

    // Fetch the customer profile associated with the provided user identifier
    @GetMapping("/{userId}")
    public ApiResponse<CustomerDTO> getCustomer(@PathVariable Long userId) {
        return customerService.getCustomerByUserId(userId);
    }
}
