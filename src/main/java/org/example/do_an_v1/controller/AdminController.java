package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.AdminStatusUpdateRequest;
import org.example.do_an_v1.dto.request.ConfirmRefundRequest;
import org.example.do_an_v1.dto.request.CustomerStatusUpdateRequest;
import org.example.do_an_v1.dto.request.HomestayApprovalRequest;
import org.example.do_an_v1.dto.request.HomestayStatusUpdateRequest;
import org.example.do_an_v1.dto.request.HostStatusUpdateRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRefundRequest;
import org.example.do_an_v1.dto.response.AdminFinanceReportResponse;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.dto.response.HomestayStatisticsDTO;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.AdminService;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;
    private final HostService hostService;
    private final RequestIdentityResolver identityResolver;

    @PostMapping("/invite")
    public ApiResponse<AdminInvitationResponse> inviteAdmin(@RequestBody @Valid AdminInviteRequest request) {
        Long actorId = identityResolver.requireUserId(null);
        return adminService.inviteAdmin(actorId, request);
    }

    @PostMapping("/activate")
    public ApiResponse<AdminDTO> activateAdmin(@RequestBody @Valid AdminActivationRequest request) {
        return adminService.activateAdmin(request);
    }

    // Admin lấy danh sách tất cả admin
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<List<AdminDTO>>> getAllAdmins(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return adminService.getAllAdmins(page, size);
    }

    // Admin lấy danh sách tất cả customer
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/customers")
    public ApiResponse<PageResponse<List<CustomerDTO>>> getAllCustomers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return adminService.getAllCustomers(page, size);
    }

    // Admin cập nhật trạng thái admin
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/status")
    public ApiResponse<AdminDTO> updateAdminStatus(@RequestBody @Valid AdminStatusUpdateRequest request) {
        return adminService.updateAdminStatus(request.getIdAdmin(), request.getStatus());
    }

    // Admin cập nhật trạng thái customer
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/customers/status")
    public ApiResponse<CustomerDTO> updateCustomerStatus(@RequestBody @Valid CustomerStatusUpdateRequest request) {
        return adminService.updateCustomerStatus(request.getIdCustomer(), request.getStatus());
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

    // Admin lấy chi tiết admin theo idAdmin
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{idAdmin}")
    public ApiResponse<AdminDTO> getAdminDetail(@PathVariable Long idAdmin) {
        return adminService.getAdminById(idAdmin);
    }

    // Admin lấy chi tiết customer theo idCustomer
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/customers/{idCustomer}")
    public ApiResponse<CustomerDTO> getCustomerDetail(@PathVariable Long idCustomer) {
        return adminService.getCustomerById(idCustomer);
    }

    // Danh sách toàn bộ bill trong hệ thống với các bộ lọc cơ bản
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/bills")
    public ApiResponse<PageResponse<List<BillDTO>>> getAllBillsForAdmin(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) StatusBill status,
            @RequestParam(name = "customerId", required = false) Long customerId,
            @RequestParam(name = "hostId", required = false) Long hostId,
            @RequestParam(name = "homestayId", required = false) Long homestayId
    ) {
        return adminService.getAllBills(page, size, status, customerId, hostId, homestayId);
    }

    // Danh sách giao dịch cho một bill
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/bills/{billId}/transactions")
    public ApiResponse<List<TransactionDTO>> getBillTransactions(@PathVariable Long billId) {
        return adminService.getTransactionsForBill(billId);
    }

    //    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/host/{userId}")
    public ApiResponse<HostDTO> getHostForAdmin(@PathVariable Long userId) {
        return hostService.getHostDetailForAdmin(userId);
    }

    // Admin cập nhật trạng thái host (nhận idHost và statusHost)
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/host/status")
    public ApiResponse<HostDTO> updateHostStatus(@RequestBody @Valid HostStatusUpdateRequest request) {
        return adminService.updateHostStatus(request.getIdHost(), request.getStatusHost());
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
     * Báo cáo nguồn thu/chi của hệ thống
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/reports/finance")
    public ApiResponse<AdminFinanceReportResponse> getFinanceReport() {
        return adminService.getFinanceReport();
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

    /**
     * Admin lấy thống kê tất cả homestays: số lượng booking, tổng tiền kiếm được, số lượng khiếu nại
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/homestays/statistics")
    public ApiResponse<List<HomestayStatisticsDTO>> getAllHomestayStatistics() {
        return adminService.getAllHomestayStatistics();
    }

    /**
     * Admin lấy danh sách tất cả complaints với phân trang
     */
    @GetMapping("/complaints")
    public ApiResponse<PageResponse<List<ComplaintDTO>>> getAllComplaints(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return adminService.getAllComplaints(page, size);
    }
}
