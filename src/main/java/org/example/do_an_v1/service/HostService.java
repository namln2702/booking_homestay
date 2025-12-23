package org.example.do_an_v1.service;

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

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface HostService {

    ApiResponse<HostDTO> registerHost(Long userId, HostRegistrationRequest request) throws RuntimeException;

    ApiResponse<PageResponse<List<HostDTO>>> getHostsForAdmin(StatusHost status, int page, int size);

    ApiResponse<HostDTO> getHostDetailForAdmin(Long hostUserId);

    ApiResponse<HostDTO> approveHost(Long adminUserId, Long hostUserId);

    ApiResponse<HostDTO> getHostByUserId(Long userId);

    /**
     * Thống kê danh sách homestay theo host (dùng userId của host)
     */
    ApiResponse<?> getHomestaysForHost(Long hostUserId);

    /**
     * Liệt kê các bill đã đặt (thành công) của các homestay thuộc host
     */
    ApiResponse<?> getBillsForHostHomestays(Long hostUserId);

    ApiResponse<?> confirmCheckin(Long hostUserId, CheckinRequest request, HttpServletRequest httpRequest);

    ApiResponse<?> confirmCheckout(CheckoutRequest request);

    /**
     * Bật ngày hoạt động của homestay
     * Nếu homestayDailyPrice đã tồn tại thì set isBooked = false
     * Nếu chưa có thì tạo mới với isBooked = false
     */
    ApiResponse<?> enableHomestayDays(Long hostUserId, UpdateHomestayPriceRequest request);

    /**
     * Tắt ngày hoạt động của homestay
     * Nếu homestayDailyPrice đã tồn tại và chưa được book thì xóa
     * Nếu đã được book thì set isBooked = true (không cho book thêm)
     */
    ApiResponse<?> disableHomestayDays(Long hostUserId, UpdateHomestayPriceRequest request);

    /**
     * Cập nhật giá homestay theo price_per_day
     * Chỉ cập nhật giá cho các ngày chưa được book
     */
    ApiResponse<?> updateHomestayPrices(Long hostUserId, UpdateHomestayPriceRequest request);

    /**
     * Host hủy bill
     * Chỉ được hủy nếu đến thời gian check-in mà customer không đến được
     * Sau 3h từ thời gian check-in, host có thể hủy và không cần hoàn tiền
     */
    ApiResponse<?> cancelBill(Long hostUserId, Long billId);

    /**
     * Host lấy danh sách các bill ở trạng thái HOST_COMPLAINT_PROCESSING
     * (các khiếu nại đang chờ host xử lý)
     */
    ApiResponse<?> getComplaintProcessingBills(Long hostUserId);

    /**
     * Host xử lý khiếu nại (đồng ý hoặc không đồng ý)
     * - Đồng ý: chuyển bill status thành REFUNDED
     * - Không đồng ý: chuyển bill status thành ADMIN_COMPLAINT_PROCESSING (để admin xử lý)
     */
    ApiResponse<?> processComplaint(Long hostUserId, ProcessComplaintRequest request);

    /**
     * Host cập nhật trạng thái homestay
     * - ACTIVE -> INACTIVE: chuyển toàn bộ bill chưa hoàn thành sang REFUNDED và tạo transaction REFUND
     * - INACTIVE -> ACTIVE: chỉ cập nhật status
     */
    ApiResponse<?> updateHomestayStatus(Long hostUserId, UpdateHomestayStatusRequest request);

    /**
     * Host lấy chi tiết bill theo billId
     * Kiểm tra bill có thuộc về homestay của host không
     */
    ApiResponse<?> getBillDetail(Long hostUserId, Long billId);

    /**
     * Host lấy tất cả các khiếu nại (complaints) của các homestay thuộc về host
     */
    ApiResponse<?> getAllComplaints(Long hostUserId);
}
