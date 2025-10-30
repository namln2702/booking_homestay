package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.payload.ApiResponse;

public interface AdminService {

    ApiResponse<AdminInvitationResponse> inviteAdmin(Long actorAdminId, AdminInviteRequest request) throws RuntimeException;

    ApiResponse<AdminDTO> activateAdmin(AdminActivationRequest request) throws RuntimeException;
}
