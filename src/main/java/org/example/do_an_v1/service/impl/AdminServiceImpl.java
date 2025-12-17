package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.dto.request.AdminActivationRequest;
import org.example.do_an_v1.dto.request.AdminInviteRequest;
import org.example.do_an_v1.dto.request.ConfirmRefundRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRefundRequest;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.*;
import org.example.do_an_v1.exception.ResourceNotFoundException;
import org.example.do_an_v1.mapper.HomestayMapper;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.dto.response.AdminInvitationResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.AdminService;
import org.example.do_an_v1.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final int INVITE_CODE_LENGTH = 6;

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final HostRepository hostRepository;
    private final HomestayRepository homestayRepository;
    private final HomestayImageRepository homestayImageRepository;
    private final ConfirmEmailRepository confirmEmailRepository;
    private final BillRepository billRepository;
    private final ComplaintRepository complaintRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final ProfileMapper profileMapper;
    private final HomestayMapper homestayMapper;

    @Override
    @Transactional
    public ApiResponse<AdminInvitationResponse> inviteAdmin(Long actorAdminId, AdminInviteRequest request) throws RuntimeException {
        Admin actingAdmin = adminRepository.findById(actorAdminId).orElse(null);
        if (Objects.isNull(actingAdmin) || actingAdmin.getLevelAdmin() != LevelAdmin.SUPER_ADMIN) {
            return new ApiResponse<>(403, "Only super admins may invite new admins", null);
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail);
        if (Objects.isNull(user)) {
            user = User.builder()
                    .email(normalizedEmail)
                    .name(request.getFullName())
                    .phone(request.getPhone())
                    .build();
        } else if (Objects.nonNull(user.getAdmin()) && Status.ACTIVE.equals(user.getAdmin().getStatus())) {
            Admin existingAdmin = user.getAdmin();
            AdminInvitationResponse conflictResponse = AdminInvitationResponse.builder()
                    .admin(profileMapper.toAdminDTO(existingAdmin))
                    .activationCode(null)
                    .build();
            return new ApiResponse<>(409,
                    "Admin account already active for email: " + normalizedEmail,
                    conflictResponse);
        } else {
            user.setName(request.getFullName());
            if (Objects.nonNull(request.getPhone())) {
                user.setPhone(request.getPhone());
            }
        }

        user = userRepository.save(user);

        Admin admin = adminRepository.findById(user.getId()).orElse(null);
        if (Objects.isNull(admin)) {
            admin = Admin.builder()
                    .user(user)
                    .status(Status.INACTIVE)
                    .levelAdmin(Objects.requireNonNullElse(request.getLevelAdmin(), LevelAdmin.ADMIN))
                    .build();
        } else {
            admin.setStatus(Status.INACTIVE);
            admin.setLevelAdmin(Objects.requireNonNullElse(request.getLevelAdmin(), admin.getLevelAdmin()));
        }
        admin.setRole(RoleUser.ADMIN);
        // BaseEntity timestamps (createdAt/updatedAt) auto-populate here; requests never control them
        admin = adminRepository.save(admin);

        String inviteCode = generateInviteCode();
        // expired_at is derived here so invitation validity stays under server control, never taken from client input
        confirmEmailRepository.save(ConfirmEmail.builder()
                .email(normalizedEmail)
                .code(inviteCode)
                .expired_at(LocalDateTime.now().plusHours(24))
                .build());

        emailService.sendSimpleEmail(normalizedEmail, buildInvitationMessage(inviteCode));

        AdminInvitationResponse response = AdminInvitationResponse.builder()
                .admin(profileMapper.toAdminDTO(admin))
                .activationCode(inviteCode)
                .build();

        return new ApiResponse<>(200,
                "Invitation sent successfully. Activation code is returned for testing purposes only.",
                response);
    }

    @Override
    @Transactional
    public ApiResponse<AdminDTO> activateAdmin(AdminActivationRequest request) throws RuntimeException {
        ConfirmEmail confirmEmail = confirmEmailRepository.findByEmailAndCode(request.getEmail(), request.getCode());

        if (Objects.isNull(confirmEmail)) {
            return new ApiResponse<>(422, "Invalid activation code", null);
        }

        if (LocalDateTime.now().isAfter(confirmEmail.getExpired_at())) {
            return new ApiResponse<>(422, "Activation code expired", null);
        }

        User user = userRepository.findByEmail(request.getEmail());
        if (Objects.isNull(user)) {
            throw new ResourceNotFoundException("User not found with email: " + request.getEmail());
        }

        Admin admin = adminRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin account not found for user id: " + user.getId()));

        if (Objects.nonNull(request.getFullName())) {
            user.setName(request.getFullName());
        }
        if (Objects.nonNull(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);

        admin.setStatus(Status.ACTIVE);
        admin = adminRepository.save(admin);

        return new ApiResponse<>(200, "Admin account activated successfully", profileMapper.toAdminDTO(admin));
    }

    private String generateInviteCode() {
        SecureRandom secureRandom = new SecureRandom();
        return IntStream.range(0, INVITE_CODE_LENGTH)
                .map(i -> secureRandom.nextInt(10))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    private String buildInvitationMessage(String code) {
        return "You have been invited to join the admin team. Use this one-time activation code within 24 hours: " + code;
    }
    @Override
    @Transactional
    public ApiResponse<HostDTO> updateHostStatus(Long idHost, StatusHost statusHost) {
        if (idHost == null) {
            throw new IllegalArgumentException("Host id is required");
        }
        if (statusHost == null) {
            return new ApiResponse<>(400, "StatusHost is required", null);
        }

        Host host = hostRepository.findById(idHost)
                .orElseThrow(() -> new IllegalArgumentException("Host not found for id " + idHost));

        host.setStatusHost(statusHost);
        Host savedHost = hostRepository.save(host);

        return new ApiResponse<>(200, "Host status updated successfully", profileMapper.toHostDTO(savedHost));
    }

    @Override
    @Transactional
    public ApiResponse<HomestayDTO> approveHomestay(Long homestayId, Boolean approve) {

        if (homestayId == null) {
            throw new IllegalArgumentException("Homestay id is required");
        }
        if (approve == null) {
            return new ApiResponse<>(400, "Approval decision is required", null);
        }

//        Admin admin = adminRepository.findById(adminUserId)
//                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for user id " + adminUserId));

//        if (admin.getStatus() != Status.ACTIVE) {
//            return new ApiResponse<>(403, "Admin account is not active", null);
//        }

        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + homestayId));

        if (homestay.getStatusHomestay() != StatusHomestay.PENDING) {
            return new ApiResponse<>(409, "Homestay is not in a pending state", null);
        }

        if (Boolean.TRUE.equals(approve)) {
            homestay.setStatusHomestay(StatusHomestay.ACTIVE);
        } else {
            homestay.setStatusHomestay(StatusHomestay.CANCEL);
        }

        Homestay savedHomestay = homestayRepository.save(homestay);

        List<HomestayImage> images = homestayImageRepository.findByHomestay(savedHomestay);
        HomestayDTO response = homestayMapper.toDto(savedHomestay, images);

        String message = Boolean.TRUE.equals(approve)
                ? "Homestay approved successfully"
                : "Homestay rejected successfully";

        return new ApiResponse<>(200, message, response);
    }

    @Override
    @Transactional
    public ApiResponse<HomestayDTO> updateHomestayStatus( Long homestayId, StatusHomestay status) {

        if (homestayId == null) {
            throw new IllegalArgumentException("Homestay id is required");
        }
        if (status == null) {
            return new ApiResponse<>(400, "StatusHomestay is required", null);
        }

        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + homestayId));

        homestay.setStatusHomestay(status);
        Homestay savedHomestay = homestayRepository.save(homestay);

        List<HomestayImage> images = homestayImageRepository.findByHomestay(savedHomestay);
        HomestayDTO response = homestayMapper.toDto(savedHomestay, images);

        return new ApiResponse<>(200, "Homestay status updated successfully", response);
    }

    private Admin requireActiveAdmin(Long adminUserId) {
        if (adminUserId == null) {
            throw new IllegalArgumentException("Admin user id is required");
        }
        Admin admin = adminRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for user id " + adminUserId));
        if (!Objects.equals(admin.getStatus(), Status.ACTIVE)) {
            return null;
        }
        return admin;
    }

    @Override
    public ApiResponse<List<TransactionDTO>> getPendingRefunds(Long adminUserId) {
        // Validate admin
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        // Lấy danh sách transaction REFUND đang chờ xử lý
        List<Transaction> pendingRefunds = transactionRepository.findByTransactionTypeAndStatus(
                TypeTransaction.REFUND,
                StatusTransaction.PENDING
        );

        // Map sang DTO
        List<TransactionDTO> refundDTOs = pendingRefunds.stream()
                .map(this::mapToTransactionDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Pending refunds retrieved successfully", refundDTOs);
    }

    @Override
    @Transactional
    public ApiResponse<?> confirmRefund(Long adminUserId, ConfirmRefundRequest request) {
        // Validate admin
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        // Tìm transaction
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElse(null);
        
        if (transaction == null) {
            return new ApiResponse<>(404, "Transaction not found with id: " + request.getTransactionId(), null);
        }

        // Validate: Transaction phải là REFUND và đang ở trạng thái PENDING
        if (transaction.getTransactionType() != TypeTransaction.REFUND) {
            return new ApiResponse<>(400, "Transaction is not a REFUND transaction", null);
        }

        if (transaction.getStatus() != StatusTransaction.PENDING) {
            return new ApiResponse<>(400, "Transaction is not in PENDING status. Current status: " + transaction.getStatus(), null);
        }

        // Cập nhật transaction: thêm proof image và chuyển status sang SUCCESS
        transaction.setProofImageUrl(request.getProofImageUrl());
        transaction.setStatus(StatusTransaction.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return new ApiResponse<>(200, "Refund confirmed successfully", mapToTransactionDTO(transaction));
    }

    @Override
    @Transactional
    public ApiResponse<?> processComplaintRefund(Long adminUserId, ProcessComplaintRefundRequest request) {
        // Validate admin
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        // Validate input
        if (request == null || request.getComplaintId() == null) {
            return new ApiResponse<>(400, "Complaint ID is required", null);
        }
        if (request.getApproved() == null) {
            return new ApiResponse<>(400, "Approval decision is required", null);
        }

        // Tìm complaint
        Complaint complaint = complaintRepository.findById(request.getComplaintId()).orElse(null);
        if (complaint == null) {
            return new ApiResponse<>(404, "Complaint not found with id: " + request.getComplaintId(), null);
        }

        // Lấy bill từ complaint
        Bill bill = complaint.getBill();
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found for this complaint", null);
        }

        // Validate: Bill phải ở trạng thái ADMIN_COMPLAINT_PROCESSING
        if (bill.getStatus() != StatusBill.ADMIN_COMPLAINT_PROCESSING) {
            return new ApiResponse<>(400, 
                    "Bill must be in ADMIN_COMPLAINT_PROCESSING status. Current status: " + bill.getStatus(), 
                    null);
        }

        // Xử lý theo quyết định
        if (Boolean.TRUE.equals(request.getApproved())) {
            // Đồng ý: chuyển bill sang REFUNDED và tạo transaction REFUND mới
            if (bill.getTotalAmount() == null) {
                return new ApiResponse<>(400, "Bill total amount is not set", null);
            }

            // Lấy admin user để tạo transaction
            User adminUser = admin.getUser();
            if (adminUser == null) {
                return new ApiResponse<>(500, "Admin user not found", null);
            }

            // Tạo transaction REFUND mới (admin -> customer)
            Transaction refundTransaction = Transaction.builder()
                    .amount(bill.getTotalAmount())
                    .transactionType(TypeTransaction.REFUND)
                    .status(StatusTransaction.SUCCESS) // Admin đã duyệt nên thành công luôn
                    .bill(bill)
                    .fromUser(adminUser)
                    .toUser(bill.getCustomer().getUser())
                    .completedAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(refundTransaction);

            // Cập nhật bill sang REFUNDED
            bill.setStatus(StatusBill.REFUNDED);
            billRepository.save(bill);

            return new ApiResponse<>(200, 
                    "Complaint approved. Bill status changed to REFUNDED. Refund transaction created.", 
                    java.util.Map.of(
                            "billId", bill.getId(),
                            "billStatus", bill.getStatus(),
                            "transactionId", refundTransaction.getId(),
                            "transactionStatus", refundTransaction.getStatus(),
                            "amount", refundTransaction.getAmount()
                    ));
        } else {
            // Từ chối: chuyển bill sang REJECTED (không tạo transaction)
            bill.setStatus(StatusBill.REJECTED);
            billRepository.save(bill);

            return new ApiResponse<>(200, 
                    "Complaint rejected. Bill status changed to REJECTED.", 
                    java.util.Map.of(
                            "billId", bill.getId(),
                            "billStatus", bill.getStatus()
                    ));
        }
    }

    /**
     * Map Transaction entity sang TransactionDTO
     */
    private TransactionDTO mapToTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .completedAt(transaction.getCompletedAt())
                .fromUserId(transaction.getFromUser() != null ? transaction.getFromUser().getId() : null)
                .fromUserEmail(transaction.getFromUser() != null ? transaction.getFromUser().getEmail() : null)
                .toUserId(transaction.getToUser() != null ? transaction.getToUser().getId() : null)
                .toUserEmail(transaction.getToUser() != null ? transaction.getToUser().getEmail() : null)
                .billId(transaction.getBill() != null ? transaction.getBill().getId() : null)
                .billCode(transaction.getBill() != null ? transaction.getBill().getCode() : null)
                .proofImageUrl(transaction.getProofImageUrl())
                .build();
    }
}
