package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.response.BillComplaintResponse;
import org.example.do_an_v1.dto.request.CheckinRequest;
import org.example.do_an_v1.dto.request.CheckoutRequest;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.dto.request.ProcessComplaintRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayPriceRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayStatusRequest;
import org.example.do_an_v1.dto.response.RevenueStatisticsResponse;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.enums.TypeTransaction;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.mapper.BillMapper;
import org.example.do_an_v1.mapper.ComplaintMapper;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.ComplaintRepository;
import org.example.do_an_v1.repository.HomestayDailyPricesRepository;
import org.example.do_an_v1.repository.HomestayRepository;
import org.example.do_an_v1.repository.HostRepository;
import org.example.do_an_v1.repository.PricePerDayRepository;
import org.example.do_an_v1.repository.TransactionRepository;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.EmailService;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.example.do_an_v1.service.support.VNPayPaymentSupport;
import org.example.do_an_v1.dto.response.VNPayPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Optional;
import org.example.do_an_v1.service.SystemConfigService;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {
    private static final List<StatusBill> STATUS_TIMELINE = List.of(
            StatusBill.DEPOSIT_PENDING,
            StatusBill.DEPOSIT_FAILED,
            StatusBill.REMAINING_PAYMENT_PENDING,
            StatusBill.REMAINING_PAYMENT_FAILED,
            StatusBill.CHECKIN_EXPIRED,
            StatusBill.COMPLAINT_PENDING,
            StatusBill.HOST_COMPLAINT_PROCESSING,
            StatusBill.ADMIN_COMPLAINT_PROCESSING,
            StatusBill.REFUNDED_PENDING,
            StatusBill.REFUNDED,
            StatusBill.REJECTED,
            StatusBill.SUCCEED,
            StatusBill.CANCELLED_REFUNDED,
            StatusBill.CANCELLED
    );

    private final HostRepository hostRepository;
    private final AdminRepository adminRepository;
    private final ProfileMapper profileMapper;
    private final UserRegistrationSupport userRegistrationSupport;
    private final BillRepository billRepository;
    private final HomestayRepository homestayRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;
    private final PricePerDayRepository pricePerDayRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final VNPayPaymentSupport vnPayPaymentSupport;
    private final SystemConfigService systemConfigService;
    private final ComplaintRepository complaintRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ApiResponse<HostDTO> registerHost(Long userId, HostRegistrationRequest request) throws RuntimeException {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required to register as host");
        }

        if (request == null) {
            throw new IllegalArgumentException("Host registration request is required");
        }

        User user = userRegistrationSupport.getUserOrThrow(userId);

        Host existingHost = hostRepository.findById(user.getId()).orElse(null);
        if (existingHost != null) {
            return new ApiResponse<>(409, "Host profile already exists for this user", profileMapper.toHostDTO(existingHost));
        }

        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                userId,
                request.getUsername(),
                request.getName(),
                request.getPhone(),
                request.getAge(),
                request.getAvatarUrl()
        );

        userRegistrationSupport.applyUserAttributes(user, userRequest);

        Host host = Host.builder()
                .user(user)
                .role(RoleUser.HOST)
                .statusHost(StatusHost.PENDING)
                .businessName(request.getBusinessName())
                .qrCodeUrl(request.getQrCodeUrl())
                .build();

        Host savedHost = hostRepository.save(host);

        return new ApiResponse<>(200, "Host profile created successfully", profileMapper.toHostDTO(savedHost));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<HostDTO>>> getHostsForAdmin(int page, int size, String status, String businessName, String email) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;
        
        // Parse status string to enum
        StatusHost statusEnum = null;
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            try {
                statusEnum = StatusHost.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        // Normalize search strings
        String normalizedBusinessName = (businessName != null && !businessName.trim().isEmpty()) 
                ? businessName.trim().toLowerCase() 
                : null;
        String normalizedEmail = (email != null && !email.trim().isEmpty()) 
                ? email.trim().toLowerCase() 
                : null;
        
        // Load all hosts and filter
        List<Host> allHosts = hostRepository.findAll();

        StatusHost finalStatusEnum = statusEnum;
        List<Host> filteredHosts = allHosts.stream()
                .filter(host -> finalStatusEnum == null || host.getStatusHost() == finalStatusEnum)
                .filter(host -> {
                    if (normalizedBusinessName == null) {
                        return true;
                    }
                    if (host.getBusinessName() == null) {
                        return false;
                    }
                    return host.getBusinessName().toLowerCase().contains(normalizedBusinessName);
                })
                .filter(host -> {
                    if (normalizedEmail == null) {
                        return true;
                    }
                    if (host.getUser() == null || host.getUser().getEmail() == null) {
                        return false;
                    }
                    return host.getUser().getEmail().toLowerCase().contains(normalizedEmail);
                })
                .collect(Collectors.toList());
        
        // Manual pagination
        long total = filteredHosts.size();
        int fromIndex = Math.min(safePage * safeSize, filteredHosts.size());
        int toIndex = Math.min(fromIndex + safeSize, filteredHosts.size());
        
        List<HostDTO> hosts = filteredHosts.subList(fromIndex, toIndex).stream()
                .map(profileMapper::toHostDTO)
                .toList();

        PageResponse<List<HostDTO>> response = PageResponse.<List<HostDTO>>builder()
                .page(safePage)
                .size(safeSize)
                .total(total)
                .items(hosts)
                .build();

        return new ApiResponse<>(200, "Hosts retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HostDTO> getHostDetailForAdmin( Long hostUserId) {

        Host host = hostRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host profile not found for user id " + hostUserId));

        return new ApiResponse<>(200, "Host detail retrieved successfully", profileMapper.toHostDTO(host));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HostDTO> getHostByUserId(Long userId) {
        Host host = hostRepository.findById(userId).orElse(null);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found", null);
        }
        return new ApiResponse<>(200, "Host profile retrieved successfully", profileMapper.toHostDTO(host));
    }

    @Override
    @Transactional
    public ApiResponse<HostDTO> approveHost(Long adminUserId, Long hostUserId) {
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        Host host = hostRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host profile not found for user id " + hostUserId));

        if (host.getStatusHost() == StatusHost.ACTIVE) {
            return new ApiResponse<>(409, "Host has already been approved", profileMapper.toHostDTO(host));
        }

        if (host.getStatusHost() != StatusHost.PENDING) {
            return new ApiResponse<>(409, "Host status must be pending before approval", profileMapper.toHostDTO(host));
        }

        host.setStatusHost(StatusHost.ACTIVE);
        if (host.getRole() != RoleUser.HOST) {
            host.setRole(RoleUser.HOST);
        }
        Host savedHost = hostRepository.save(host);
        return new ApiResponse<>(200, "Host approved successfully", profileMapper.toHostDTO(savedHost));
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
    @Transactional(readOnly = true)
    public ApiResponse<?> getHomestaysForHost(Long hostUserId) {
        if (hostUserId == null) {
            throw new IllegalArgumentException("Host user id is required");
        }

        User user = userRegistrationSupport.getUserOrThrow(hostUserId);
        Host host = hostRepository.findByUser(user);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found for this user", null);
        }

        List<Homestay> homestays = homestayRepository.findByHost(host);

        List<HomestaySummaryDTO> summaries = homestays.stream()
                .map(h -> {
                    // Tìm ảnh chính (isPrimary = true) từ danh sách ảnh của homestay
                    String primaryImageUrl = null;
                    if (h.getListImage() != null) {
                        primaryImageUrl = h.getListImage().stream()
                                .filter(img -> img != null && Boolean.TRUE.equals(img.getIsPrimary()))
                                .map(Image::getImage_url)
                                .findFirst()
                                .orElse(null);
                        
                        // Nếu không có ảnh chính, lấy ảnh đầu tiên
                        if (primaryImageUrl == null && !h.getListImage().isEmpty()) {
                            Image firstImage = h.getListImage().iterator().next();
                            primaryImageUrl = firstImage != null ? firstImage.getImage_url() : null;
                        }
                    }
                    
                    return HomestaySummaryDTO.builder()
                            .id(h.getId())
                            .title(h.getTitle())
                            .category(h.getCategory())
                            .status(h.getStatusHomestay())
                            .hostId(host.getId())
                            .hostName(user.getName())
                            .city(h.getAddress() != null ? h.getAddress().getCity() : null)
                            .state(h.getAddress() != null ? h.getAddress().getState() : null)
                            .primaryImageUrl(primaryImageUrl)
                            .createdAt(h.getCreatedAt())
                            .build();
                })
                .toList();

        return new ApiResponse<>(200, "Homestays for host retrieved successfully", summaries);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getBillsForHostHomestays(Long hostUserId) {
        if (hostUserId == null) {
            throw new IllegalArgumentException("Host user id is required");
        }

        User user = userRegistrationSupport.getUserOrThrow(hostUserId);
        Host host = hostRepository.findByUser(user);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found for this user", null);
        }

        List<Bill> bills = billRepository.findByHomestay_Host(host);
//
//        // Lọc các bill đã/đang được sử dụng (logic tương tự history customer)
//        List<Bill> filtered = bills.stream()
//                .filter(bill -> {
//                    StatusBill status = bill.getStatus();
//                    return status == StatusBill.SUCCEED
////                            || status == StatusBill.COMPLAINT_EXPIRED
//                            || status == StatusBill.CHECKIN_EXPIRED
//                            || status == StatusBill.COMPLAINT_PENDING
//                            || status == StatusBill.CHECKIN_PENDING;
//                })
//                .toList();

        List<BillDTO> billDTOS = bills.stream()
                .map(BillMapper::toDTO)
                .toList();

        return new ApiResponse<>(200, "Bills for host homestays retrieved successfully", billDTOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getComplaintProcessingBills(Long hostUserId) {
        if (hostUserId == null) {
            return new ApiResponse<>(400, "Host user ID is required", null);
        }

        // Tìm host
        Host host = hostRepository.findById(hostUserId).orElse(null);
        if (host == null) {
            return new ApiResponse<>(404, "Host not found with id: " + hostUserId, null);
        }

        // Lấy tất cả bills của các homestay thuộc host này
        List<Bill> allBills = billRepository.findByHomestay_Host(host);

        // Lọc các bill ở trạng thái HOST_COMPLAINT_PROCESSING
        List<Bill> complaintProcessingBills = allBills.stream()
                .filter(bill -> bill.getStatus() == StatusBill.HOST_COMPLAINT_PROCESSING)
                .toList();

        // Map sang BillComplaintResponse - chỉ chứa thông tin bill và complaint
        List<BillComplaintResponse> responses = complaintProcessingBills.stream()
                .map(bill -> {
                    // Tìm complaint của bill này
                    Complaint complaint = complaintRepository
                            .findTopByBillOrderByCreatedAtDesc(bill)
                            .orElse(null);
                    
                    // Map complaint sang DTO
                    ComplaintDTO complaintDTO = complaint != null 
                            ? ComplaintMapper.toDTO(complaint)
                            : null;
                    
                    // Tạo response chỉ với bill info và complaint info
                    return BillComplaintResponse.builder()
                            .billId(bill.getId())
                            .billCode(bill.getCode())
                            .billStatus(bill.getStatus())
                            .checkIn(bill.getCheckIn())
                            .checkOut(bill.getCheckOut())
                            .actualCheckinTime(bill.getActualCheckinTime())
                            .totalAmount(bill.getTotalAmount())
                            .billCreatedAt(bill.getCreatedAt())
                            .complaint(complaintDTO)
                            .build();
                })
                .toList();

        log.info("Retrieved {} complaint processing bills for host {}", responses.size(), hostUserId);

        return new ApiResponse<>(200, 
                String.format("Retrieved %d complaint processing bills", responses.size()), 
                responses);
    }

    @Override
    @Transactional
    public ApiResponse<?> processComplaint(Long hostUserId, ProcessComplaintRequest request) {
        // Validate input
        if (hostUserId == null) {
            return new ApiResponse<>(400, "Host user ID is required", null);
        }
        if (request == null) {
            return new ApiResponse<>(400, "Request is required", null);
        }
        if (request.getComplaintId() == null) {
            return new ApiResponse<>(400, "Complaint ID is required", null);
        }
        if (request.getApproved() == null) {
            return new ApiResponse<>(400, "Approval status is required", null);
        }

        // Tìm host
        Host host = hostRepository.findById(hostUserId).orElse(null);
        if (host == null) {
            return new ApiResponse<>(404, "Host not found with id: " + hostUserId, null);
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

        // Validate: Bill phải thuộc về homestay của host này
        if (bill.getHomestay() == null || bill.getHomestay().getHost() == null 
                || !Objects.equals(bill.getHomestay().getHost().getId(), host.getId())) {
            return new ApiResponse<>(403, "You can only process complaints for bills of your own homestays", null);
        }

        // Validate: Bill phải ở trạng thái HOST_COMPLAINT_PROCESSING
        if (bill.getStatus() != StatusBill.HOST_COMPLAINT_PROCESSING) {
            return new ApiResponse<>(400, 
                    "Bill must be in HOST_COMPLAINT_PROCESSING status to process complaint. Current status: " + bill.getStatus(), 
                    null);
        }

        Complaint latestComplaint = complaintRepository
                .findTopByBillOrderByCreatedAtDesc(bill)
                .orElse(null);
        if (latestComplaint == null || !Objects.equals(latestComplaint.getId(), complaint.getId())) {
            return new ApiResponse<>(400, "Complaint is no longer active for this bill", null);
        }

        // Lưu trạng thái cũ để gửi email
        StatusBill oldStatus = bill.getStatus();
        String customerEmail = bill.getCustomer() != null && bill.getCustomer().getUser() != null 
                ? bill.getCustomer().getUser().getEmail() 
                : null;

        // Xử lý theo quyết định của host
        if (request.getApproved()) {
            // Host đồng ý -> chuyển thành REFUNDED và tạo transaction REFUND
            // Nếu bill chưa có actual_checkout, set nó = thời điểm hiện tại và unlock các ngày còn lại
            handleRefundPendingForActiveStay(bill);
            
            bill.setStatus(StatusBill.REFUNDED_PENDING);
            billRepository.save(bill);

            // Tạo transaction REFUND (admin -> customer)
            // Lấy admin user
            User adminUser = adminRepository.findAll().stream()
                    .map(Admin::getUser)
                    .findFirst()
                    .orElse(null);

            if (adminUser != null && bill.getTotalAmount() != null) {
                Transaction refundTransaction = Transaction.builder()
                        .amount(bill.getTotalAmount())
                        .transactionType(TypeTransaction.REFUND)
                        .status(StatusTransaction.PENDING) // Chờ admin xác nhận
                        .bill(bill)
                        .fromUser(adminUser)
                        .toUser(bill.getCustomer().getUser())
                        .completedAt(null) // Chưa hoàn tất, chờ admin xác nhận
                        .build();
                transactionRepository.save(refundTransaction);
            }

            // Gửi email thông báo cho customer
            if (customerEmail != null) {
                try {
                    emailService.sendComplaintStatusEmail(
                            customerEmail,
                            bill.getCode(),
                            oldStatus.toString(),
                            StatusBill.REFUNDED_PENDING.toString(),
                            "Host"
                    );
                    log.info("Complaint status email sent to customer {} for bill {}", customerEmail, bill.getId());
                } catch (Exception e) {
                    log.error("Failed to send complaint status email to customer {} for bill {}: {}", 
                            customerEmail, bill.getId(), e.getMessage());
                }
            }

            log.info("Host {} approved complaint {} for bill {}. Bill status changed to REFUNDED.", 
                    hostUserId, request.getComplaintId(), bill.getId());

            return new ApiResponse<>(200, 
                    "Complaint approved. Bill status changed to REFUNDED. Refund transaction created.", 
                    null);
        } else {
            // Host không đồng ý -> chuyển thành ADMIN_COMPLAINT_PROCESSING (để admin xử lý)
            bill.setStatus(StatusBill.ADMIN_COMPLAINT_PROCESSING);
            billRepository.save(bill);

            // Gửi email thông báo cho customer
            if (customerEmail != null) {
                try {
                    emailService.sendComplaintStatusEmail(
                            customerEmail,
                            bill.getCode(),
                            oldStatus.toString(),
                            StatusBill.ADMIN_COMPLAINT_PROCESSING.toString(),
                            "Host"
                    );
                    log.info("Complaint status email sent to customer {} for bill {}", customerEmail, bill.getId());
                } catch (Exception e) {
                    log.error("Failed to send complaint status email to customer {} for bill {}: {}", 
                            customerEmail, bill.getId(), e.getMessage());
                }
            }

            log.info("Host {} rejected complaint {} for bill {}. Bill status changed to ADMIN_COMPLAINT_PROCESSING.", 
                    hostUserId, request.getComplaintId(), bill.getId());

            return new ApiResponse<>(200, 
                    "Complaint rejected. Bill status changed to ADMIN_COMPLAINT_PROCESSING. Admin will review.", 
                    null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> confirmCheckin(Long hostUserId, CheckinRequest request, HttpServletRequest httpRequest) {
        if (hostUserId == null) {
            return new ApiResponse<>(400, "Host user ID is required", null);
        }
        if (request == null || request.getBillId() == null) {
            return new ApiResponse<>(400, "Bill ID is required", null);
        }

        Bill bill = billRepository.findById(request.getBillId()).orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + request.getBillId(), null);
        }

        String submittedCode = request.getCode() != null ? request.getCode().trim() : null;
        if (submittedCode == null || submittedCode.isEmpty()) {
            return new ApiResponse<>(400, "Check-in code is required", null);
        }
        if (!bill.getCode().equalsIgnoreCase(submittedCode)) {
            return new ApiResponse<>(400, "Invalid check-in code for this bill", null);
        }

        // Validate: Bill phải có homestay
        if (bill.getHomestay() == null) {
            return new ApiResponse<>(400, "Bill must have a homestay associated", null);
        }

        // Validate: Host phải sở hữu homestay này
        Long homestayHostId = bill.getHomestay().getHost().getId();
        if (!Objects.equals(homestayHostId, hostUserId)) {
            return new ApiResponse<>(403, "Host can only check-in customers for their own homestays", null);
        }

        // Validate: Bill phải ở trạng thái REMAINING_PAYMENT_PENDING
        if (bill.getStatus() != StatusBill.REMAINING_PAYMENT_PENDING) {
            return new ApiResponse<>(400, "Bill must be in REMAINING_PAYMENT_PENDING status to confirm checkin. Current status: " + bill.getStatus(), null);
        }

        if (bill.getCheckIn() == null) {
            return new ApiResponse<>(400, "Bill does not have check-in date", null);
        }

        // Thời điểm hiện tại (có giờ, phút, giây)
//        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDateTime checkInTime = bill.getCheckIn();

        // Thời điểm cuối ngày check-in: 23:59:59
        LocalDateTime endOfCheckInDay = checkInTime
                .toLocalDate()
                .atTime(23, 59, 59);

        // Điều kiện hợp lệ:
        // now >= checkInTime && now <= endOfCheckInDay
//        if (LocalDateTime.now().isBefore(checkInTime) || LocalDateTime.now().isAfter(endOfCheckInDay)) {
//            return new ApiResponse<>(
//                    400,
//                    "Check-in is allowed only from "
//                            + checkInTime
//                            + " until 23:59:59 of the same day",
//                    null
//            );
//        }


        // Tính 70% còn lại cần thanh toán
        if (bill.getTotalAmount() == null) {
            return new ApiResponse<>(400, "Bill total amount is not set", null);
        }

        // Tính 70% còn lại và làm tròn đến 2 chữ số thập phân
        BigDecimal remainingAmount = bill.getTotalAmount()
                .multiply(BigDecimal.valueOf(70))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Tạo transaction mới cho phần còn lại (70%)
        Transaction remainingPaymentTransaction = null;
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // Lấy admin user
            User adminUser = adminRepository.findAll().stream()
                    .map(Admin::getUser)
                    .findFirst()
                    .orElse(null);

            if (adminUser == null) {
                return new ApiResponse<>(500, "No admin user found for transaction", null);
            }

            remainingPaymentTransaction = Transaction.builder()
                    .completedAt(LocalDateTime.now().plusMinutes(15))
                    .transactionType(TypeTransaction.CUSTOMER_PAYMENT_ADMIN_SECOND)
                    .status(StatusTransaction.PENDING)
                    .bill(bill)
                    .fromUser(bill.getCustomer().getUser())
                    .toUser(adminUser)
                    .amount(remainingAmount)
                    .build();
            transactionRepository.save(remainingPaymentTransaction);
        }

        // Cập nhật trạng thái bill thành REMAINING_PAYMENT_PENDING (cần thanh toán phần còn lại)
        bill.setStatus(StatusBill.REMAINING_PAYMENT_PENDING);
        bill.setActualCheckinTime(LocalDateTime.now());
        billRepository.save(bill);

        // Tạo payment URL cho phần 70% còn lại nếu có transaction
        if (remainingPaymentTransaction != null && httpRequest != null) {
            VNPayPaymentResponse paymentResponse = vnPayPaymentSupport.createPaymentUrlForTransaction(
                    remainingPaymentTransaction,
                    httpRequest
            );

            if (paymentResponse != null) {
                // Trả về response với payment URL
                return new ApiResponse<>(200, 
                        "Check-in confirmed successfully. Please pay the remaining 70% of the bill.", 
                        java.util.Map.of(
                                "paymentUrl", paymentResponse.getPaymentUrl(),
                                "orderId", paymentResponse.getOrderId(),
                                "amount", paymentResponse.getAmount(),
                                "message", "Please complete the payment for the remaining 70%"
                        )
                );
            } else {
                // Nếu có lỗi khi tạo payment URL, vẫn trả về success nhưng không có payment URL
                log.error("Error creating payment URL for remaining payment. Transaction ID: {}", 
                        remainingPaymentTransaction.getId());
                return new ApiResponse<>(200, 
                        "Check-in confirmed successfully. Please pay the remaining 70% of the bill. " +
                        "Note: Payment URL generation failed. Please contact support.", 
                        null);
            }
        }

        // Nếu không có remaining payment hoặc không có httpRequest, trả về response thông thường
        return new ApiResponse<>(200, "Check-in confirmed successfully. Please pay the remaining 70% of the bill.", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> confirmCheckout(CheckoutRequest request) {
        if (request == null || request.getBillId() == null) {
            return new ApiResponse<>(400, "Bill ID is required", null);
        }

        Bill bill = billRepository.findById(request.getBillId()).orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + request.getBillId(), null);
        }

        // Validate: Bill phải ở trạng thái sau khi checkin
        if (!isStatusAtOrAfter(bill.getStatus(), StatusBill.COMPLAINT_PENDING)) {
            return new ApiResponse<>(400,
                    "Bill must reach COMPLAINT_PENDING (post check-in) before checkout. Current status: " + bill.getStatus(),
                    null);
        }

        // Validate: Phải sau ngày check-in mới được checkout
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkInTime = bill.getActualCheckinTime() != null 
                ? bill.getActualCheckinTime() 
                : bill.getCheckIn();
        
        if (now.isBefore(checkInTime) || now.isEqual(checkInTime)) {
            return new ApiResponse<>(400, "Cannot checkout before or on check-in date. Check-in time: " + checkInTime, null);
        }

        // Validate: Host phải sở hữu homestay này
        // (Có thể thêm validation này nếu cần)

        // Cập nhật thời gian checkout thực tế
        bill.setActualCheckoutTime(now);

        // Unlock homestay_daily_prices - các ngày đã book giờ có thể book lại
        // Reload bill để có collection đầy đủ
        bill = billRepository.findById(bill.getId()).orElse(bill);
        
        if (bill.getListHomestayDailyPrices() != null) {
            for (HomestayDailyPrice dailyPrice : bill.getListHomestayDailyPrices()) {
                dailyPrice.setIsBooked(false);
                homestayDailyPricesRepository.save(dailyPrice);
            }
            // Xóa tất cả quan hệ ManyToMany
            bill.getListHomestayDailyPrices().clear();
        }

        // Cập nhật trạng thái bill thành SUCCEED
//        bill.setStatus(StatusBill.SUCCEED);
        billRepository.save(bill);

//        log.info("Check-out confirmed for bill {}. Unlocked {} daily prices.", bill.getId(), dailyPrices.size());

        return new ApiResponse<>(200, "Check-out confirmed successfully. Daily prices unlocked.", null);
    }

    private boolean isStatusAtOrAfter(StatusBill status, StatusBill reference) {
        if (status == null || reference == null) {
            return false;
        }
        int currentIndex = STATUS_TIMELINE.indexOf(status);
        int referenceIndex = STATUS_TIMELINE.indexOf(reference);
        if (currentIndex == -1 || referenceIndex == -1) {
            return false;
        }
        return currentIndex >= referenceIndex;
    }

    @Override
    @Transactional
    public ApiResponse<?> enableHomestayDays(Long hostUserId, UpdateHomestayPriceRequest request) {
        try {
            // Validate homestay thuộc về host
            Homestay homestay = homestayRepository.findById(request.getHomestayId()).orElse(null);
            if (homestay == null) {
                return new ApiResponse<>(404, "Homestay not found with id: " + request.getHomestayId(), null);
            }

            // Kiểm tra homestay thuộc về host
            if (homestay.getHost() == null || homestay.getHost().getUser() == null 
                    || !homestay.getHost().getUser().getId().equals(hostUserId)) {
                return new ApiResponse<>(403, "You don't have permission to manage this homestay", null);
            }

            // Xử lý từng ngày trong danh sách
            for (UpdateHomestayPriceRequest.DailyPriceUpdate priceUpdate : request.getDailyPrices()) {
                Date day = priceUpdate.getDay();
                Float price = priceUpdate.getPrice();

                // Tìm hoặc tạo PricePerDay
                PricePerDay pricePerDay = pricePerDayRepository.findByDay(day).orElse(null);
                if (pricePerDay == null) {
                    pricePerDay = PricePerDay.builder()
                            .day(day)
                            .price(price)
                            .build();
                    pricePerDay = pricePerDayRepository.save(pricePerDay);
                } else {
                    // Cập nhật giá nếu khác
                    if (!pricePerDay.getPrice().equals(price)) {
                        pricePerDay.setPrice(price);
                        pricePerDayRepository.save(pricePerDay);
                    }
                }

                // Tìm HomestayDailyPrice đã tồn tại
                Optional<HomestayDailyPrice> existingDailyPrice = homestayDailyPricesRepository
                        .findOneByHomestayAndPricePerDay(homestay, pricePerDay);

                if (existingDailyPrice.isPresent()) {
                    HomestayDailyPrice dailyPrice = existingDailyPrice.get();
                    
                    // Kiểm tra xem có phải do host tắt không (activeHost = true)
                    // Chỉ cho phép enable những ngày do chính host tắt
                    if (!Boolean.TRUE.equals(dailyPrice.getActiveHost())) {
                        // Không phải do host tắt, không thể enable
                        return new ApiResponse<>(409, "Cannot enable this day. It was not disabled by host or already booked by customer", null);
                    }
                    
                    // Đã tồn tại và do host tắt: set isBooked = false (bật lại) và activeHost = false
                    dailyPrice.setIsBooked(false);
                    dailyPrice.setActiveHost(false);
                    dailyPrice.setPrice(price);
                    // Xóa bill nếu có (unlock) - tìm và remove khỏi collection của bill
                    List<Bill> billsContainingDailyPrice = billRepository.findAll().stream()
                            .filter(b -> b.getListHomestayDailyPrices() != null 
                                    && b.getListHomestayDailyPrices().contains(dailyPrice))
                            .toList();
                    for (Bill b : billsContainingDailyPrice) {
                        b.getListHomestayDailyPrices().remove(dailyPrice);
                        billRepository.save(b);
                    }
                    homestayDailyPricesRepository.save(dailyPrice);
                } else {
                    // Chưa tồn tại: tạo mới với isBooked = false và activeHost = false
                    HomestayDailyPrice newDailyPrice = HomestayDailyPrice.builder()
                            .homestay(homestay)
                            .pricePerDay(pricePerDay)
                            .price(price)
                            .isBooked(false)
                            .activeHost(false)
                            .build();
                    homestayDailyPricesRepository.save(newDailyPrice);
                }
            }

            return new ApiResponse<>(200, "Homestay days enabled successfully", null);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Error enabling homestay days: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> disableHomestayDays(Long hostUserId, UpdateHomestayPriceRequest request) {
        try {
            // Validate homestay thuộc về host
            Homestay homestay = homestayRepository.findById(request.getHomestayId()).orElse(null);
            if (homestay == null) {
                return new ApiResponse<>(404, "Homestay not found with id: " + request.getHomestayId(), null);
            }

            // Kiểm tra homestay thuộc về host
            if (homestay.getHost() == null || homestay.getHost().getUser() == null 
                    || !homestay.getHost().getUser().getId().equals(hostUserId)) {
                return new ApiResponse<>(403, "You don't have permission to manage this homestay", null);
            }

            // Xử lý từng ngày trong danh sách
            for (UpdateHomestayPriceRequest.DailyPriceUpdate priceUpdate : request.getDailyPrices()) {
                Date day = priceUpdate.getDay();
                Float price = priceUpdate.getPrice();

                // Tìm hoặc tạo PricePerDay
                PricePerDay pricePerDay = pricePerDayRepository.findByDay(day).orElse(null);
                if (pricePerDay == null) {
                    // Nếu chưa có PricePerDay thì tạo mới
                    pricePerDay = PricePerDay.builder()
                            .day(day)
                            .price(price != null ? price : homestay.getBasePrice())
                            .build();
                    pricePerDay = pricePerDayRepository.save(pricePerDay);
                }

                // Tìm HomestayDailyPrice đã tồn tại
                Optional<HomestayDailyPrice> existingDailyPrice = homestayDailyPricesRepository
                        .findOneByHomestayAndPricePerDay(homestay, pricePerDay);

                if (existingDailyPrice.isPresent()) {
                    // Nếu đã có HomestayDailyPrice thì set isBooked = true và activeHost = true
                    HomestayDailyPrice dailyPrice = existingDailyPrice.get();
                    dailyPrice.setIsBooked(true);
                    dailyPrice.setActiveHost(true);
                    homestayDailyPricesRepository.save(dailyPrice);
                } else {
                    // Nếu chưa có thì tạo mới HomestayDailyPrice với isBooked = true và activeHost = true
                    HomestayDailyPrice newDailyPrice = HomestayDailyPrice.builder()
                            .homestay(homestay)
                            .pricePerDay(pricePerDay)
                            .price(price != null ? price : homestay.getBasePrice())
                            .isBooked(true)
                            .activeHost(true)
                            .build();
                    homestayDailyPricesRepository.save(newDailyPrice);
                }
            }

            return new ApiResponse<>(200, "Homestay days disabled successfully", null);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Error disabling homestay days: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> updateHomestayPrices(Long hostUserId, UpdateHomestayPriceRequest request) {
        try {
            // Validate request
            if (request == null || request.getDailyPrices() == null || request.getDailyPrices().isEmpty()) {
                return new ApiResponse<>(400, "Daily prices list is required", null);
            }

            // Validate homestay thuộc về host
            Homestay homestay = homestayRepository.findById(request.getHomestayId()).orElse(null);
            if (homestay == null) {
                return new ApiResponse<>(404, "Homestay not found with id: " + request.getHomestayId(), null);
            }

            // Kiểm tra homestay thuộc về host
            if (homestay.getHost() == null || homestay.getHost().getUser() == null 
                    || !homestay.getHost().getUser().getId().equals(hostUserId)) {
                return new ApiResponse<>(403, "You don't have permission to manage this homestay", null);
            }

            // Xử lý từng daily price trong request
            for (UpdateHomestayPriceRequest.DailyPriceUpdate priceUpdate : request.getDailyPrices()) {
                if (priceUpdate.getDay() == null || priceUpdate.getPrice() == null) {
                    continue; // Bỏ qua nếu thiếu thông tin
                }

                Date day = priceUpdate.getDay();
                Float price = priceUpdate.getPrice();

                // Tìm hoặc tạo PricePerDay
                PricePerDay pricePerDay = pricePerDayRepository.findByDay(day).orElse(null);
                if (pricePerDay == null) {
                    pricePerDay = PricePerDay.builder()
                            .day(day)
                            .price(price)
                            .build();
                    pricePerDay = pricePerDayRepository.save(pricePerDay);
                } else {
                    // Cập nhật giá nếu khác
                    if (!pricePerDay.getPrice().equals(price)) {
                        pricePerDay.setPrice(price);
                        pricePerDayRepository.save(pricePerDay);
                    }
                }

                // Tìm HomestayDailyPrice hiện có cho homestay và pricePerDay này
                Optional<HomestayDailyPrice> existingDailyPrice = homestayDailyPricesRepository
                        .findOneByHomestayAndPricePerDay(homestay, pricePerDay);

                if (existingDailyPrice.isPresent()) {
                    // Cập nhật giá nếu đã tồn tại (chỉ cập nhật nếu chưa được booked)
                    HomestayDailyPrice dailyPrice = existingDailyPrice.get();
                    // Kiểm tra xem có bill nào chứa dailyPrice không
                    boolean isInAnyBill = billRepository.findAll().stream()
                            .anyMatch(b -> b.getListHomestayDailyPrices() != null 
                                    && b.getListHomestayDailyPrices().contains(dailyPrice));
                    if (!Boolean.TRUE.equals(dailyPrice.getIsBooked()) && !isInAnyBill) {
                        dailyPrice.setPrice(price);
                        homestayDailyPricesRepository.save(dailyPrice);
                    }
                } else {
                    // Tạo mới HomestayDailyPrice
                    HomestayDailyPrice newDailyPrice = HomestayDailyPrice.builder()
                            .price(price)
                            .isBooked(false)
                            .activeHost(false)
                            .pricePerDay(pricePerDay)
                            .homestay(homestay)
                            .build();
                    homestayDailyPricesRepository.save(newDailyPrice);
                }
            }

            return new ApiResponse<>(200, "Homestay prices updated successfully", null);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Error updating homestay prices: " + e.getMessage(), null);
        }
    }

    // Trạng thái bill đã hoàn thành (không cần refund khi ẩn homestay)
    private static final EnumSet<StatusBill> COMPLETED_BILL_STATUSES = EnumSet.of(
            StatusBill.DEPOSIT_FAILED,
            StatusBill.REMAINING_PAYMENT_FAILED,
            StatusBill.CHECKIN_EXPIRED,
            StatusBill.CANCELLED,
            StatusBill.CANCELLED_REFUNDED,
            StatusBill.SUCCEED,
            StatusBill.REFUNDED,
            StatusBill.REJECTED

    );

    @Override
    @Transactional
    public ApiResponse<?> updateHomestayStatus(Long hostUserId, UpdateHomestayStatusRequest request) {
        if (hostUserId == null) {
            return new ApiResponse<>(400, "Host user ID is required", null);
        }
        if (request == null || request.getHomestayId() == null) {
            return new ApiResponse<>(400, "Homestay ID is required", null);
        }
        if (request.getStatus() == null) {
            return new ApiResponse<>(400, "Status is required", null);
        }

        // Validate host
        Host host = hostRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host not found for user id " + hostUserId));

        // Validate homestay thuộc về host này
        Homestay homestay = homestayRepository.findById(request.getHomestayId())
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + request.getHomestayId()));

        if (homestay.getHost() == null || !Objects.equals(homestay.getHost().getId(), host.getId())) {
            return new ApiResponse<>(403, "You can only update status of your own homestays", null);
        }

        StatusHomestay currentStatus = homestay.getStatusHomestay();
        StatusHomestay newStatus = request.getStatus();

        // Nếu status không thay đổi
        if (currentStatus == newStatus) {
            return new ApiResponse<>(200, "Homestay status is already " + newStatus, null);
        }

        // Xử lý khi ACTIVE -> INACTIVE (luồng giống với admin BAN/INACTIVE)
        if (currentStatus == StatusHomestay.ACTIVE && newStatus == StatusHomestay.INACTIVE) {
            List<Bill> bills = billRepository.findByHomestay(homestay);
            LocalDateTime now = LocalDateTime.now();
            
            // Lọc các bills có checkIn trong tương lai và status = REMAINING_PAYMENT_PENDING
            List<Bill> affectedBills = bills.stream()
                    .filter(bill -> bill.getCheckIn() != null && bill.getCheckIn().isAfter(now))
                    .filter(bill -> bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING)
                    .toList();

            if (!affectedBills.isEmpty()) {
                // Xử lý từng bill bị ảnh hưởng
                int refundedCount = 0;
                for (Bill bill : affectedBills) {
                    // Chuyển bill sang CANCELLED_REFUNDED
                    bill.setStatus(StatusBill.CANCELLED_REFUNDED);
                    billRepository.save(bill);

                    // Kiểm tra xem bill đã thanh toán một phần chưa (có transaction CUSTOMER_PAYMENT_ADMIN_FIRST thành công)
                    List<Transaction> billTransactions = transactionRepository.findByBill(bill);
                    Transaction firstPayment = billTransactions.stream()
                            .filter(t -> t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST
                                    && t.getStatus() == StatusTransaction.SUCCESS)
                            .findFirst()
                            .orElse(null);

                    // Nếu đã thanh toán đợt 1, tạo transaction REFUND
                    if (firstPayment != null && bill.getCustomer() != null && bill.getCustomer().getUser() != null) {
                        // Kiểm tra xem đã có transaction REFUND cho bill này chưa
                        boolean hasRefund = billTransactions.stream()
                                .anyMatch(t -> t.getTransactionType() == TypeTransaction.REFUND);

                        if (!hasRefund) {
                            // Tạo transaction REFUND với số tiền đã thanh toán
                            Transaction refundTransaction = Transaction.builder()
                                    .amount(firstPayment.getAmount())
                                    .transactionType(TypeTransaction.REFUND)
                                    .status(StatusTransaction.PENDING)
                                    .toUser(bill.getCustomer().getUser())
                                    .bill(bill)
                                    .build();
                            transactionRepository.save(refundTransaction);
                            refundedCount++;
                        }
                    }
                }

                // Cập nhật status homestay
                homestay.setStatusHomestay(StatusHomestay.INACTIVE);
                homestayRepository.save(homestay);

                return new ApiResponse<>(200, 
                        String.format("Homestay status updated to INACTIVE. %d bills cancelled and refunded.", refundedCount), 
                        null);
            }
        }

        // Xử lý khi INACTIVE -> ACTIVE (hoặc các trường hợp khác)
        if (currentStatus == StatusHomestay.INACTIVE && newStatus == StatusHomestay.ACTIVE) {
            homestay.setStatusHomestay(StatusHomestay.ACTIVE);
            homestayRepository.save(homestay);
            return new ApiResponse<>(200, "Homestay status updated to ACTIVE successfully", null);
        }

        // Các trường hợp khác: chỉ cập nhật status
        homestay.setStatusHomestay(newStatus);
        homestayRepository.save(homestay);
        return new ApiResponse<>(200, "Homestay status updated successfully", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getBillDetail(Long hostUserId, Long billId) {
        if (hostUserId == null) {
            return new ApiResponse<>(400, "Host user ID is required", null);
        }
        if (billId == null) {
            return new ApiResponse<>(400, "Bill ID is required", null);
        }

        // Validate host
        Host host = hostRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host not found for user id " + hostUserId));

        // Tìm bill
        Bill bill = billRepository.findById(billId)
                .orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + billId, null);
        }

        // Kiểm tra bill có thuộc về homestay của host này không
        if (bill.getHomestay() == null || bill.getHomestay().getHost() == null ) {
            return new ApiResponse<>(403, "Bill does not belong to any homestay", null);
        }

        if (!Objects.equals(bill.getHomestay().getHost().getId(), host.getId())) {
            return new ApiResponse<>(403, "You can only view bills for your own homestays", null);
        }

        // Map sang DTO
        BillDTO billDTO = BillMapper.toDTO(bill);

        return new ApiResponse<>(200, "Bill detail retrieved successfully", billDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllComplaints(Long hostUserId) {
        if (hostUserId == null) {
            throw new IllegalArgumentException("Host user ID is required");
        }

        Host host = hostRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host profile not found for user id " + hostUserId));

        // Lấy tất cả complaints của host (qua bills và homestays)
        List<Complaint> complaints = complaintRepository.findByHostId(host.getId());

        // Map sang DTO
        List<ComplaintDTO> complaintDTOs = complaints.stream()
                .map(ComplaintMapper::toDTO)
                .toList();

        return new ApiResponse<>(200, "Host complaints retrieved successfully", complaintDTOs);
    }

    /**
     * Xử lý khi bill chuyển sang REFUNDED_PENDING trong thời gian lưu trú
     * - Unlock các ngày còn lại từ thời điểm hiện tại (khi hủy bill) trở lên đến checkOut ban đầu
     * - Chỉ unlock những ngày trong tương lai (từ thời điểm hiện tại trở đi)
     */
    private void handleRefundPendingForActiveStay(Bill bill) {
        if (bill == null || bill.getHomestay() == null) {
            return;
        }

        // Lấy thời điểm hiện tại (thời điểm hủy bill)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime originalCheckOut = bill.getCheckOut();
        
        // Chỉ unlock nếu checkOut ban đầu chưa đến
        if (originalCheckOut != null && now.isBefore(originalCheckOut)) {
            // Nếu bill chưa có actual_checkout_time, set nó = thời điểm hiện tại
            if (bill.getActualCheckoutTime() == null) {
                bill.setActualCheckoutTime(now);
                billRepository.save(bill);
                log.info("Set actual_checkout_time for bill {} to current time", bill.getId());
            }

            // Reload bill để có collection đầy đủ
            bill = billRepository.findById(bill.getId()).orElse(bill);
            
            // Duyệt từ ngày mai (now + 1) đến checkOut
            LocalDate nowLocalDate = now.toLocalDate();
            LocalDate checkOutLocalDate = originalCheckOut.toLocalDate();
            LocalDate startDate = nowLocalDate.plusDays(1); // Từ ngày mai
            
            int unlockedCount = 0;
            
            // Duyệt từ startDate đến checkOutLocalDate
            for (LocalDate date = startDate; !date.isAfter(checkOutLocalDate); date = date.plusDays(1)) {
                // Tìm HomestayDailyPrice trong collection của bill theo ngày cụ thể
                if (bill.getListHomestayDailyPrices() != null) {
                    for (HomestayDailyPrice dailyPrice : new java.util.ArrayList<>(bill.getListHomestayDailyPrices())) {
                        // Kiểm tra xem dailyPrice có ngày bằng date không (so sánh LocalDate)
                        if (dailyPrice.getPricePerDay() != null 
                                && dailyPrice.getPricePerDay().getDay() != null) {
                            // Chuyển đổi Date sang LocalDate để so sánh
                            LocalDate dailyPriceLocalDate;
                            Date dayDate = dailyPrice.getPricePerDay().getDay();
                            if (dayDate instanceof java.sql.Date) {
                                // Nếu là java.sql.Date, dùng toLocalDate() trực tiếp
                                dailyPriceLocalDate = ((java.sql.Date) dayDate).toLocalDate();
                            } else {
                                // Nếu là java.util.Date, chuyển đổi qua Instant
                                dailyPriceLocalDate = dayDate.toInstant()
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate();
                            }
                            
                            if (dailyPriceLocalDate.equals(date)) {
                                // Unlock: set isBooked = false và remove khỏi collection
                                dailyPrice.setIsBooked(false);
                                bill.getListHomestayDailyPrices().remove(dailyPrice);
                                homestayDailyPricesRepository.save(dailyPrice);
                                unlockedCount++;
                            }
                        }
                    }
                }
            }
            
            // Lưu bill để cập nhật quan hệ ManyToMany
            billRepository.save(bill);
            
            log.info("Unlocked {} daily prices for bill {} from {} (tomorrow) to {}", 
                    unlockedCount, bill.getId(), startDate, checkOutLocalDate);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RevenueStatisticsResponse> getRevenueStatistics(
            Long hostUserId, Integer startMonth, Integer startYear, Integer endMonth, Integer endYear) {
        // Validate các tham số
        if (startMonth == null || startYear == null || endMonth == null || endYear == null) {
            return new ApiResponse<>(400, "startMonth, startYear, endMonth, and endYear are required", null);
        }

        // Validate month
        if (startMonth < 1 || startMonth > 12 || endMonth < 1 || endMonth > 12) {
            return new ApiResponse<>(400, "Month must be between 1 and 12", null);
        }

        // Validate year
        if (startYear < 2000 || startYear > 2100 || endYear < 2000 || endYear > 2100) {
            return new ApiResponse<>(400, "Year must be between 2000 and 2100", null);
        }

        // Validate host
        if (hostUserId == null) {
            return new ApiResponse<>(400, "Host user ID is required", null);
        }

        User user = userRegistrationSupport.getUserOrThrow(hostUserId);
        Host host = hostRepository.findByUser(user);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found for this user", null);
        }

        // Tính toán khoảng thời gian: từ tháng/năm bắt đầu đến tháng/năm kết thúc
        YearMonth startYearMonth = YearMonth.of(startYear, startMonth);
        YearMonth endYearMonth = YearMonth.of(endYear, endMonth);

        // Validate: endDate phải >= startDate
        if (endYearMonth.isBefore(startYearMonth)) {
            return new ApiResponse<>(400, "End date must be greater than or equal to start date", null);
        }

        // Ngày bắt đầu: ngày 1 của tháng bắt đầu (00:00:00)
        LocalDate startDate = startYearMonth.atDay(1);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        // Ngày kết thúc: ngày cuối cùng của tháng kết thúc (23:59:59.999)
        LocalDate endDate = endYearMonth.atEndOfMonth();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999_999_999);

        // Lấy tất cả bills của host này
        List<Bill> allHostBills = billRepository.findByHomestay_Host(host);

        // Lọc bills theo khoảng thời gian (updatedAt hoặc createdAt)
        List<Bill> filteredBills = allHostBills.stream()
                .filter(bill -> {
                    LocalDateTime billUpdatedAt = bill.getUpdatedAt();
                    LocalDateTime billCreatedAt = bill.getCreatedAt();
                    return (billUpdatedAt != null && !billUpdatedAt.isBefore(startDateTime) && !billUpdatedAt.isAfter(endDateTime))
                            || (billCreatedAt != null && !billCreatedAt.isBefore(startDateTime) && !billCreatedAt.isAfter(endDateTime));
                })
                .toList();

        // Lấy tất cả transactions từ các bills đã lọc
        List<Transaction> filteredTransactions = new ArrayList<>();
        for (Bill bill : filteredBills) {
            List<Transaction> billTransactions = transactionRepository.findByBill(bill);
            filteredTransactions.addAll(billTransactions);
        }

        // Lấy commission rate từ system config (tỷ lệ từ 0-1, ví dụ: 0.1 = 10%)
        BigDecimal commissionRate = systemConfigService.getConfigValueAsBigDecimal("ADMIN_COMMISSION_RATE")
                .orElse(BigDecimal.ZERO);

        // 1. Số tiền đã trả cho customer (từ transaction REFUND với status SUCCESS)
        BigDecimal customerRefundPaid = filteredTransactions.stream()
                .filter(t -> t.getTransactionType() == TypeTransaction.REFUND)
                .filter(t -> t.getStatus() == StatusTransaction.SUCCESS)
                .filter(t -> t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Số tiền chưa trả cho customer (các bill có status REFUNDED_PENDING hoặc CANCEL_REFUND_PENDING)
        BigDecimal customerRefundPending = filteredBills.stream()
                .filter(bill -> bill.getStatus() == StatusBill.REFUNDED_PENDING
                        || bill.getStatus() == StatusBill.CANCEL_REFUND_PENDING)
                .filter(bill -> bill.getTotalAmount() != null)
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách bills có status SUCCEED, REJECTED, CANCELLED, CHECKIN_EXPIRED
        List<Bill> completedPayoutBills = filteredBills.stream()
                .filter(bill -> bill.getStatus() == StatusBill.SUCCEED
                        || bill.getStatus() == StatusBill.REJECTED
                        || bill.getStatus() == StatusBill.CANCELLED
                        || bill.getStatus() == StatusBill.CHECKIN_EXPIRED)
                .toList();

        // 3. Số tiền host dự định sẽ nhận được trong tháng
        // (Từ các bill có status SUCCEED, REJECTED, CANCELLED, CHECKIN_EXPIRED
        // chưa có transaction ADMIN_PAYMENT_HOST hoặc có transaction với status PENDING)
        BigDecimal hostExpectedAmount = BigDecimal.ZERO;
        for (Bill bill : completedPayoutBills) {
            List<Transaction> billTransactions = transactionRepository.findByBill(bill);
            List<Transaction> payoutTransactions = billTransactions.stream()
                    .filter(t -> t.getTransactionType() == TypeTransaction.ADMIN_PAYMENT_HOST)
                    .toList();

            // Nếu chưa có transaction ADMIN_PAYMENT_HOST
            if (payoutTransactions.isEmpty()) {
                // Tính từ totalReceived * (1 - commissionRate)
                BigDecimal totalReceived = calculateTotalReceivedAmount(bill);
                BigDecimal payoutAmount = totalReceived.multiply(BigDecimal.ONE.subtract(commissionRate))
                        .setScale(2, RoundingMode.HALF_UP);
                if (payoutAmount.compareTo(BigDecimal.ZERO) > 0) {
                    hostExpectedAmount = hostExpectedAmount.add(payoutAmount);
                }
            } else {
                // Nếu có transaction, kiểm tra xem có transaction PENDING không
                Transaction pendingPayoutTransaction = payoutTransactions.stream()
                        .filter(t -> t.getStatus() == StatusTransaction.PENDING)
                        .findFirst()
                        .orElse(null);

                if (pendingPayoutTransaction != null && pendingPayoutTransaction.getAmount() != null) {
                    // Có transaction PENDING → lấy amount từ transaction
                    hostExpectedAmount = hostExpectedAmount.add(pendingPayoutTransaction.getAmount());
                }
                // Nếu chỉ có transaction SUCCESS → bỏ qua (đã nhận rồi)
            }
        }

        // 4. Số tiền host đã nhận được
        // (Từ các bill có status SUCCEED, REJECTED, CANCELLED, CHECKIN_EXPIRED
        // và có transaction ADMIN_PAYMENT_HOST với status SUCCESS)
        BigDecimal hostReceivedAmount = BigDecimal.ZERO;
        for (Bill bill : completedPayoutBills) {
            List<Transaction> billTransactions = transactionRepository.findByBill(bill);
            Transaction successPayoutTransaction = billTransactions.stream()
                    .filter(t -> t.getTransactionType() == TypeTransaction.ADMIN_PAYMENT_HOST)
                    .filter(t -> t.getStatus() == StatusTransaction.SUCCESS)
                    .findFirst()
                    .orElse(null);

            if (successPayoutTransaction != null && successPayoutTransaction.getAmount() != null) {
                hostReceivedAmount = hostReceivedAmount.add(successPayoutTransaction.getAmount());
            }
        }

        // 5. Số tiền host dự kiến nhận được (tương tự hostExpectedAmount, từ transaction PENDING)
        BigDecimal hostPendingAmount = hostExpectedAmount;

        RevenueStatisticsResponse statistics = RevenueStatisticsResponse.builder()
                .customerRefundPaid(customerRefundPaid)
                .customerRefundPending(customerRefundPending)
                .hostExpectedAmount(hostExpectedAmount)
                .adminExpectedCommission(BigDecimal.ZERO) // Không tính cho host
                .adminReceivedFromCustomer(BigDecimal.ZERO) // Không tính cho host
                .commission(commissionRate)
                .hostReceivedAmount(hostReceivedAmount)
                .hostPendingAmount(hostPendingAmount)
                .hostId(host.getId())
                .build();

        return new ApiResponse<>(200, 
                String.format("Revenue statistics retrieved successfully for host %d, period: %s to %s", 
                        host.getId(), startDateTime.toLocalDate(), endDateTime.toLocalDate()), 
                statistics);
    }

    /**
     * Tính tổng tiền đã nhận từ customer cho bill này
     * Bao gồm CUSTOMER_PAYMENT_ADMIN_FIRST và CUSTOMER_PAYMENT_ADMIN_SECOND thành công
     */
    private BigDecimal calculateTotalReceivedAmount(Bill bill) {
        List<Transaction> billTransactions = transactionRepository.findByBill(bill);

        return billTransactions.stream()
                .filter(t -> (t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST
                        || t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN_SECOND
                        || t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN)
                        && t.getStatus() == StatusTransaction.SUCCESS)
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
