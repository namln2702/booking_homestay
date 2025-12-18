package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.ConfirmRefundRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRefundRequest;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface AdminService {

    ApiResponse<AdminInvitationResponse> inviteAdmin(Long actorAdminId, AdminInviteRequest request) throws RuntimeException;

    ApiResponse<AdminDTO> activateAdmin(AdminActivationRequest request) throws RuntimeException;

    ApiResponse<HostDTO> updateHostStatus(Long idHost, StatusHost statusHost);

    ApiResponse<HomestayDTO> approveHomestay(Long homestayId, Boolean approve);

    ApiResponse<HomestayDTO> updateHomestayStatus(Long homestayId, StatusHomestay status);

    /**
     * Lấy danh sách tất cả admin
     */
    ApiResponse<PageResponse<List<AdminDTO>>> getAllAdmins(int page, int size);

    /**
     * Lấy danh sách tất cả customer
     */
    ApiResponse<PageResponse<List<CustomerDTO>>> getAllCustomers(int page, int size);

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
     * Lấy danh sách các transaction REFUND đang chờ xử lý
     */
    ApiResponse<List<TransactionDTO>> getPendingRefunds(Long adminUserId);

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
}
