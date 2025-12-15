package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.ConfirmRefundRequest;
import org.example.do_an_v1.dto.request.HomestayApprovalRequest;
import org.example.do_an_v1.dto.request.HomestayStatusUpdateRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRefundRequest;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.AdminService;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;
    private final HostService hostService;
    private final RequestIdentityResolver identityResolver;

//    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
//    @GetMapping("/homestays")
//    public ApiResponse<PageResponse<List<HomestaySummaryDTO>>> listHomestays(
//            @RequestParam(name = "status", required = false, defaultValue = "PENDING") StatusHomestay status,
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "20") int size
//    ) {
//        Long actorId = identityResolver.requireUserId(null);
//        return homestayService.getHomestays(status, page, size);
//    }
//
//    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
//    @GetMapping("/homestays/{homestayId}")
//    public ApiResponse<HomestayDTO> getHomestayDetail(@PathVariable Long homestayId) {
//        Long actorId = identityResolver.requireUserId(null);
//        return homestayService.getHomestayDetailForAdmin(actorId, homestayId);
//    }

//    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PostMapping("/invite")
    public ApiResponse<AdminInvitationResponse> inviteAdmin(@RequestBody @Valid AdminInviteRequest request) {
        Long actorId = identityResolver.requireUserId(null);
        return adminService.inviteAdmin(actorId, request);
    }
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<HostDTO> getHostForAdmin(@PathVariable Long userId) {
        Long adminUserId = identityResolver.requireUserId(null);
        return hostService.getHostDetailForAdmin(adminUserId, userId);
    }
    @PostMapping("/activate")
    public ApiResponse<AdminDTO> activateAdmin(@RequestBody @Valid AdminActivationRequest request) {
        return adminService.activateAdmin(request);
    }

    // Admins approve or reject homestays submitted by hosts
//    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PutMapping("/homestays/approve")
    public ApiResponse<HomestayDTO> approveHomestay(@RequestBody @Valid HomestayApprovalRequest request) {
        return adminService.approveHomestay(request.getHomestayId(), request.getApprove());
    }

    // Admin update arbitrary homestay status by id and StatusHomestay
//    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PutMapping("/homestays/status")
    public ApiResponse<HomestayDTO> updateHomestayStatus(@RequestBody @Valid HomestayStatusUpdateRequest request) {

        return adminService.updateHomestayStatus(request.getHomestayId(), request.getStatus());
    }

//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/{userId}/approve")
    public ApiResponse<HostDTO> approveHost(@PathVariable Long userId) {
        Long adminUserId = identityResolver.requireUserId(null);
        return adminService.approveHost(adminUserId, userId);
    }

    /**
     * Lấy danh sách các transaction REFUND đang chờ xử lý
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/refunds/pending")
    public ApiResponse<List<TransactionDTO>> getPendingRefunds() {
        Long adminUserId = identityResolver.requireUserId(null);
        return adminService.getPendingRefunds(adminUserId);
    }

    /**
     * Admin xác nhận hoàn tiền thành công
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     * @param request Thông tin xác nhận (transactionId và proofImageUrl)
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping("/refunds/confirm")
    public ApiResponse<?> confirmRefund(@RequestBody @Valid ConfirmRefundRequest request) {
        Long adminUserId = identityResolver.requireUserId(null);
        return adminService.confirmRefund(adminUserId, request);
    }

    /**
     * Admin phê duyệt complaint: chuyển bill từ ADMIN_COMPLAINT_PROCESSING sang REFUNDED hoặc REJECTED
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping("/complaints/process-refund")
    public ApiResponse<?> processComplaintRefund(@RequestBody @Valid ProcessComplaintRefundRequest request) {
        Long adminUserId = identityResolver.requireUserId(null);
        return adminService.processComplaintRefund(adminUserId, request);
    }
}
