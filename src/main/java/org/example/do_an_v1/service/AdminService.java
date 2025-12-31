package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.dto.request.UpdateStatusAdminRequest;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.AdminLoginRequest;
import org.example.do_an_v1.dto.request.ConfirmRefundRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRefundRequest;
import org.example.do_an_v1.dto.response.AdminFinanceReportResponse;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.dto.response.HomestayStatisticsDTO;
import org.example.do_an_v1.dto.response.HostWithPendingPayoutTransactionsResponse;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface AdminService {

    /**
     * Admin login với username và password
     * @param request Thông tin đăng nhập (username từ User, password từ Admin)
     * @return AccessTokenSystemDTO chứa token và thông tin admin
     */
    ApiResponse<?> login(AdminLoginRequest request);

    ApiResponse<AdminInvitationResponse> createAdmin( AdminInviteRequest request) throws RuntimeException;

    ApiResponse<AdminDTO> updateStatusAdmin(UpdateStatusAdminRequest request) throws RuntimeException;

    ApiResponse<HostDTO> updateHostStatus(Long idHost, StatusHost statusHost);

    ApiResponse<HomestayDTO> approveHomestay(Long homestayId, Boolean approve);

    ApiResponse<HomestayDTO> updateHomestayStatus(Long homestayId, StatusHomestay status);

    /**
     * Lấy danh sách tất cả admin
     */
    ApiResponse<PageResponse<List<AdminDTO>>> getAllAdmins(int page, int size, String status, String email);

    /**
     * Lấy danh sách tất cả customer
     */
    ApiResponse<PageResponse<List<CustomerDTO>>> getAllCustomers(int page, int size, String status, String email);

    /**
     * Cập nhật trạng thái admin
     */
    ApiResponse<AdminDTO> updateAdminStatus(Long idAdmin, Status status);

    /**
     * Cập nhật trạng thái customer
     */
    ApiResponse<CustomerDTO> updateCustomerStatus(Long idCustomer, Status status);

    /**
     * Lấy chi tiết admin theo idAdmin
     */
    ApiResponse<AdminDTO> getAdminById(Long idAdmin);

    /**
     * Lấy chi tiết customer theo idCustomer
     */
    ApiResponse<CustomerDTO> getCustomerById(Long idCustomer);

    /**
     * Lấy danh sách bill toàn hệ thống với bộ lọc cơ bản
     */
    ApiResponse<PageResponse<List<BillDTO>>> getAllBills(
            int page,
            int size,
            StatusBill status,
            Long customerId,
            Long hostId,
            Long homestayId,
            String billCode,
            String customerName,
            String homestayTitle
    );

    /**
     * Lấy danh sách giao dịch của một bill cụ thể
     */
    ApiResponse<List<TransactionDTO>> getTransactionsForBill(Long billId);

    /**
     * Lấy danh sách các transaction REFUND đang chờ xử lý
     */
    ApiResponse<List<TransactionDTO>> getPendingRefunds(Long adminUserId);

    /**
     * Lấy danh sách các transaction trả tiền cho host (PAYLOAD_HOST, ADMIN_PAYMENT_HOST)
     * filter theo status (optional)
     */
    ApiResponse<List<TransactionDTO>> getHostPayoutTransactions(StatusTransaction status);

    /**
     * Admin xác nhận hoàn tiền thành công
     * @param adminUserId ID của admin thực hiện
     * @param request Thông tin xác nhận (transactionId và proofImageUrl)
     */
    ApiResponse<?> confirmRefund(Long adminUserId, ConfirmRefundRequest request);

    /**
     * Admin phê duyệt complaint: chuyển bill từ ADMIN_COMPLAINT_PROCESSING sang REFUNDED hoặc REJECTED
     * @param adminUserId ID của admin thực hiện
     * @param request Thông tin quyết định (complaintId, approved, proofImageUrl)
     */
    ApiResponse<?> processComplaintRefund(Long adminUserId, ProcessComplaintRefundRequest request);

    /**
     * Báo cáo tổng hợp dòng tiền từ customer và chi cho host
     */
    ApiResponse<AdminFinanceReportResponse> getFinanceReport();

    /**
     * Lấy thống kê tất cả homestays: số lượng booking, tổng tiền kiếm được, số lượng khiếu nại
     * @return Danh sách thống kê của tất cả homestays
     */
    ApiResponse<List<HomestayStatisticsDTO>> getAllHomestayStatistics();

    /**
     * Lấy danh sách tất cả complaints với phân trang
     */
    ApiResponse<PageResponse<List<ComplaintDTO>>> getAllComplaints(int page, int size);

    /**
     * Lấy danh sách các Host với các Homestay và các transaction đang chờ hoàn tiền
     * (transaction type là thanh toán cho host và status là PENDING)
     */
    ApiResponse<List<HostWithPendingPayoutTransactionsResponse>> getHostsWithPendingPayoutTransactions();
}
