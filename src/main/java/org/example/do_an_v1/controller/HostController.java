package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.CheckinRequest;
import org.example.do_an_v1.dto.request.CheckoutRequest;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayPriceRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayStatusRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hosts")
public class HostController {

    private final HostService hostService;
    private final RequestIdentityResolver identityResolver;

    @PostMapping
    public ApiResponse<HostDTO> registerHost(@RequestBody @Valid HostRegistrationRequest request) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return hostService.registerHost(effectiveUserId, request);
    }

//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<List<HostDTO>>> listHosts(
            @RequestParam(name = "status", required = false) StatusHost status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return hostService.getHostsForAdmin(status, page, size);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/me")
    public ApiResponse<HostDTO> getMyHostProfile() {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return hostService.getHostByUserId(effectiveUserId);
    }

    /**
     * Thống kê danh sách homestay thuộc về host hiện tại
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/homestays")
    public ApiResponse<?> getMyHomestays() {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.getHomestaysForHost(hostUserId);
    }

    /**
     * Liệt kê các bill đã đặt (thành công/đang sử dụng) cho các homestay của host hiện tại
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/bills")
    public ApiResponse<?> getMyHomestayBills() {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.getBillsForHostHomestays(hostUserId);
    }

    // Admin-only: fetch host profile for a specific user id




    /**
     * Xác nhận check-in thành công
     * Yêu cầu quyền HOST
     * Host chỉ có thể check-in cho các bill thuộc về homestay của mình
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/checkin")
    public ApiResponse<?> confirmCheckin(@RequestBody @Valid CheckinRequest request,
                                        jakarta.servlet.http.HttpServletRequest httpRequest) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.confirmCheckin(hostUserId, request, httpRequest);
    }

    /**
     * Xác nhận trả phòng thành công
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/checkout")
    public ApiResponse<?> confirmCheckout(@RequestBody @Valid CheckoutRequest request) {
        return hostService.confirmCheckout(request);
    }

    /**
     * Bật ngày hoạt động của homestay
     * Nếu homestayDailyPrice đã tồn tại thì set isBooked = false
     * Nếu chưa có thì tạo mới với isBooked = false
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/homestays/enable-days")
    public ApiResponse<?> enableHomestayDays(@RequestBody @Valid UpdateHomestayPriceRequest request) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.enableHomestayDays(hostUserId, request);
    }

    /**
     * Tắt ngày hoạt động của homestay
     * Nếu homestayDailyPrice đã tồn tại và chưa được book thì xóa
     * Nếu đã được book thì set isBooked = true (không cho book thêm)
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/homestays/disable-days")
    public ApiResponse<?> disableHomestayDays(@RequestBody @Valid UpdateHomestayPriceRequest request) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.disableHomestayDays(hostUserId, request);
    }

    /**
     * Cập nhật giá homestay theo price_per_day
     * Chỉ cập nhật giá cho các ngày chưa được book
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PutMapping("/homestays/prices")
    public ApiResponse<?> updateHomestayPrices(@RequestBody @Valid UpdateHomestayPriceRequest request) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.updateHomestayPrices(hostUserId, request);
    }

    /**
     * Host hủy bill
     * Chỉ được hủy nếu đến thời gian check-in mà customer không đến được
     * Sau 3h từ thời gian check-in, host có thể hủy và không cần hoàn tiền
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/bills/{billId}/cancel")
    public ApiResponse<?> cancelBill(@PathVariable Long billId) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        return hostService.cancelBill(effectiveUserId, billId);
    }

    /**
     * Host lấy danh sách các bill ở trạng thái HOST_COMPLAINT_PROCESSING
     * (các khiếu nại đang chờ host xử lý)
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/bills/complaint-processing")
    public ApiResponse<?> getComplaintProcessingBills() {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.getComplaintProcessingBills(hostUserId);
    }

    /**
     * Host xử lý khiếu nại (đồng ý hoặc không đồng ý)
     * - Đồng ý: chuyển bill status thành REFUNDED và tạo transaction REFUND
     * - Không đồng ý: chuyển bill status thành ADMIN_COMPLAINT_PROCESSING (để admin xử lý)
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("/complaints/process")
    public ApiResponse<?> processComplaint(@RequestBody @Valid ProcessComplaintRequest request) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.processComplaint(hostUserId, request);
    }

    /**
     * Host cập nhật trạng thái homestay
     * - ACTIVE -> INACTIVE: chuyển toàn bộ bill chưa hoàn thành sang REFUNDED và tạo transaction REFUND
     * - INACTIVE -> ACTIVE: chỉ cập nhật status
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PutMapping("/homestays/status")
    public ApiResponse<?> updateHomestayStatus(@RequestBody @Valid UpdateHomestayStatusRequest request) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.updateHomestayStatus(hostUserId, request);
    }

    /**
     * Host lấy chi tiết bill theo billId
     * Kiểm tra bill có thuộc về homestay của host không
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/{billId}")
    public ApiResponse<?> getBillDetail(@PathVariable Long billId) {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.getBillDetail(hostUserId, billId);
    }

    /**
     * Host lấy tất cả các khiếu nại (complaints) của các homestay thuộc về host
     * Yêu cầu quyền HOST
     */
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/complaints")
    public ApiResponse<?> getAllComplaints() {
        Long hostUserId = identityResolver.requireUserId(null);
        return hostService.getAllComplaints(hostUserId);
    }

    //  Cap nhap lai token host
    // @PreAuthorize("hasAuthority('ROLE_HOST')")
    // @PostMapping("/me/refresh-token")
    // public ApiResponse<String> refreshTokenForHost() {
    //     Long effectiveUserId = identityResolver.requireUserId(null);
    //     return hostService.refreshTokenForHost(effectiveUserId);
    // }

//    @PreAuthorize("hasAuthority('ROLE_HOST')")
//    @PostMapping("/me/homestays")
//    public ApiResponse<HomestayDTO> createHomestayForCurrentHost(
//            @RequestBody @Valid HomestayCreateRequest request
//    ) {
//        Long effectiveUserId = identityResolver.requireUserId(null);
//        System.out.println("HostController.createHomestayForCurrentHost: " + effectiveUserId );
//        return homestayService.createHomestay(effectiveUserId, request);
//    }
}
