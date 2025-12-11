package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.ConfirmRefundRequest;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface AdminService {

    ApiResponse<AdminInvitationResponse> inviteAdmin(Long actorAdminId, AdminInviteRequest request) throws RuntimeException;

    ApiResponse<AdminDTO> activateAdmin(AdminActivationRequest request) throws RuntimeException;

    ApiResponse<HostDTO> approveHost(Long adminUserId, Long hostUserId);

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
}
