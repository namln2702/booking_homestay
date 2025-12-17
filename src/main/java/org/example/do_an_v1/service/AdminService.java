package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.ConfirmRefundRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRefundRequest;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface AdminService {

    ApiResponse<AdminInvitationResponse> inviteAdmin(Long actorAdminId, AdminInviteRequest request) throws RuntimeException;

    ApiResponse<AdminDTO> activateAdmin(AdminActivationRequest request) throws RuntimeException;

    ApiResponse<HostDTO> updateHostStatus(Long idHost, StatusHost statusHost);

    ApiResponse<HomestayDTO> approveHomestay(Long homestayId, Boolean approve);

    ApiResponse<HomestayDTO> updateHomestayStatus(Long homestayId, StatusHomestay status);

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
