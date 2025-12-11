package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.BookingDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.configuration.SessionConfig;
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
    private final SessionConfig sessionConfig;

    /**
     * register a new customer
     */
    @PostMapping
    public ApiResponse<CustomerDTO> upsertProfileCustomer(@RequestBody @Valid CustomerDTO customerDTO) {
        Long effectiveUserId = identityResolver.requireUserId(customerDTO.getIdUser());
        customerDTO.setIdUser(effectiveUserId);
        return customerService.upsertCustomerProfile(customerDTO);
    }

    // Fetch the authenticated customer's profile
    @GetMapping("/me")
    public ApiResponse<CustomerDTO> getMyCustomer() {
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
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PostMapping("/booking")
    ApiResponse<?> booking(@RequestBody @Valid BookingDTO bookingDTO){
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.booking(effectiveUserId, bookingDTO);
    }


    // create review
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PostMapping("/user/review")
    public ApiResponse<?> reviewHomestay(@RequestBody @Valid ReviewDTO reviewDTO){
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.reviewHomestay(effectiveUserId, reviewDTO);
    }


    // update review
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PutMapping("/user/review/{reviewId}")
    public ApiResponse<?> updateReviewHomestay(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewDTO reviewDTO
    ){
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.updateReviewHomestay(effectiveUserId, reviewId, reviewDTO);
    }

    //update Preference for Customer
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PutMapping("/user/preference")
    public ApiResponse<?> updatePreferenceCustomer(@RequestBody @Valid CustomerDTO customerDTO){
        Long userId = (Long) sessionConfig.httpSession().getAttribute("id");
        return customerService.updatePreferencesCustomer(userId, customerDTO);
    }

    /**
     * Thống kê các bill đã đặt của Customer
     * Yêu cầu quyền CUSTOMER
     */
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me/bills/all")
    public ApiResponse<?> getMyBills() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.getCustomerBills(effectiveUserId);
    }

    /**
     * Customer hủy bill
     * Nếu hủy trước 2 ngày so với check-in thì được hoàn tiền
     * Nếu muộn hơn thì không được hoàn tiền
     */
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PostMapping("/bills/{billId}/cancel")
    public ApiResponse<?> cancelBill(@PathVariable Long billId) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.cancelBill(effectiveUserId, billId);
    }

}
