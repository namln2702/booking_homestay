package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.BookingDTO;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.CustomerProfileWithTierDTO;
import org.example.do_an_v1.dto.CustomerTierDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.dto.request.CancelComplaintRequest;
import org.example.do_an_v1.dto.request.CustomerProfileUpdateRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.configuration.SessionConfig;
import org.example.do_an_v1.service.CustomerService;
import org.example.do_an_v1.service.CustomerTierService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerTierService customerTierService;
    private final RequestIdentityResolver identityResolver;
    private final SessionConfig sessionConfig;

    /**
     * Customer updates their own profile
     */
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PutMapping("/me")
    public ApiResponse<?> upsertProfileCustomer(@RequestBody CustomerProfileUpdateRequest request) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.upsertCustomerProfile(effectiveUserId, request);
    }

    // Fetch the authenticated customer's profile including derived tier
    @GetMapping("/me")
    public ApiResponse<?> getMyCustomer() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        ApiResponse<CustomerDTO> profileResponse = customerService.getCustomerByUserId(effectiveUserId);
        if (profileResponse == null || profileResponse.getStatus() != 200 || profileResponse.getData() == null) {
            return profileResponse;
        }
        ApiResponse<CustomerTierDTO> tierResponse = customerTierService.getCustomerTier(effectiveUserId);
        if (tierResponse == null || tierResponse.getStatus() != 200 || tierResponse.getData() == null) {
            return tierResponse;
        }
        CustomerProfileWithTierDTO combined = CustomerProfileWithTierDTO.builder()
                .customer(profileResponse.getData())
                .tier(tierResponse.getData())
                .build();
        return new ApiResponse<>(200, "Customer profile retrieved", combined);
    }

    // Admin-only: fetch the customer profile associated with the provided user identifier
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<CustomerDTO> getCustomerForAdmin(@PathVariable Long userId) {
        Long effectiveUserId = identityResolver.requireUserId(userId);
        return customerService.getCustomerByUserId(effectiveUserId);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_HOST','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{customerId}/tier")
    public ApiResponse<CustomerTierDTO> getCustomerTier(@PathVariable Long customerId) {
        Long effectiveUserId = identityResolver.requireUserId(customerId);
        return customerTierService.getCustomerTier(effectiveUserId);
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

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @GetMapping("/user/reviews")
    public ApiResponse<List<ReviewDTO>> getMyReviews() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.getCustomerReviews(effectiveUserId);
    }

    //update Preference for Customer
//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PutMapping("/user/preference")
    public ApiResponse<?> updatePreferenceCustomer(@RequestBody @Valid CustomerDTO customerDTO){
        Long userId = (Long) sessionConfig.httpSession().getAttribute("id");
        return customerService.updatePreferencesCustomer(8L, customerDTO);
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

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me/orders")
    public ApiResponse<?> getMyOrders() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.getCustomerOrders(effectiveUserId);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me/orders/{billId}")
    public ApiResponse<?> getMyOrderDetail(@PathVariable Long billId) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.getCustomerOrderDetail(effectiveUserId, billId);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me/complaints")
    public ApiResponse<?> getMyComplaints() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.getCustomerComplaints(effectiveUserId);
    }

    /**
     * Customer hủy bill
     * Nếu hủy trước 2 ngày so với check-in thì được hoàn tiền
     * Nếu muộn hơn thì không được hoàn tiền
     */
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PostMapping("/bills/cancel/{billId}")
    public ApiResponse<?> cancelBill(@PathVariable(name = "billId") Long billId) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.cancelBill(effectiveUserId, billId);
    }

    /**
     * Customer tạo khiếu nại
     * Chỉ có thể khiếu nại trong thời gian (N + 1) ngày sau checkout
     * N là số ngày đặt phòng (từ checkIn đến checkOut)
     */
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PostMapping("/complaints")
    public ApiResponse<?> createComplaint(@RequestBody @Valid ComplaintDTO complaintDTO) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.createComplaint(effectiveUserId, complaintDTO);
    }

    /**
     * Customer cập nhật khiếu nại
     * Chỉ có thể update khi complaint chưa được xử lý bởi admin
     */
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    @PutMapping("/complaints/{complaintId}")
    public ApiResponse<?> updateComplaint(
            @PathVariable Long complaintId,
            @RequestBody @Valid ComplaintDTO complaintDTO
    ) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.updateComplaint(effectiveUserId, complaintId, complaintDTO);
    }

    /**
     * Customer hủy khiếu nại hiện tại của bill
     */
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/bills/{billId}/complaint/cancel")
    public ApiResponse<?> cancelComplaint(
            @PathVariable Long billId,
            @RequestBody(required = false) @Valid CancelComplaintRequest request
    ) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return customerService.cancelComplaint(effectiveUserId, billId, request);
    }

}
