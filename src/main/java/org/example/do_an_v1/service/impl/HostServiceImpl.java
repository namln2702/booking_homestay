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
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.Complaint;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.example.do_an_v1.entity.PricePerDay;
import org.example.do_an_v1.entity.Transaction;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.enums.TypeTransaction;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.entity.Bill;
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
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.example.do_an_v1.service.support.VNPayPaymentSupport;
import org.example.do_an_v1.dto.response.VNPayPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {
    private static final List<StatusBill> STATUS_TIMELINE = List.of(
            StatusBill.DEPOSIT_PENDING,
            StatusBill.DEPOSIT_PAID,
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
    private final ComplaintRepository complaintRepository;

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

        return new ApiResponse<>(201, "Host profile created successfully", profileMapper.toHostDTO(savedHost));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<HostDTO>>> getHostsForAdmin(StatusHost status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Host> hostPage = status != null
                ? hostRepository.findByStatusHost(status, pageable)
                : hostRepository.findAll(pageable);

        List<HostDTO> hosts = hostPage.getContent().stream()
                .map(profileMapper::toHostDTO)
                .toList();

        PageResponse<List<HostDTO>> response = PageResponse.<List<HostDTO>>builder()
                .page(hostPage.getNumber())
                .size(hostPage.getSize())
                .total(hostPage.getTotalElements())
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
                .map(h -> HomestaySummaryDTO.builder()
                        .id(h.getId())
                        .title(h.getTitle())
                        .category(h.getCategory())
                        .status(h.getStatusHomestay())
                        .hostId(host.getId())
                        .hostName(user.getName())
                        .city(h.getAddress() != null ? h.getAddress().getCity() : null)
                        .state(h.getAddress() != null ? h.getAddress().getState() : null)
                        .createdAt(h.getCreatedAt())
                        .build())
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

        // Xử lý theo quyết định của host
        if (request.getApproved()) {
            // Host đồng ý -> chuyển thành REFUNDED và tạo transaction REFUND
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

            log.info("Host {} approved complaint {} for bill {}. Bill status changed to REFUNDED.", 
                    hostUserId, request.getComplaintId(), bill.getId());

            return new ApiResponse<>(200, 
                    "Complaint approved. Bill status changed to REFUNDED. Refund transaction created.", 
                    null);
        } else {
            // Host không đồng ý -> chuyển thành ADMIN_COMPLAINT_PROCESSING (để admin xử lý)
            bill.setStatus(StatusBill.ADMIN_COMPLAINT_PROCESSING);
            billRepository.save(bill);

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

        LocalDate today = LocalDate.now();
        LocalDate checkInDate = bill.getCheckIn().toLocalDate();
        if (!today.isEqual(checkInDate)) {
            return new ApiResponse<>(400, "Check-in is only allowed on the scheduled date: " + checkInDate, null);
        }

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
        List<HomestayDailyPrice> dailyPrices = homestayDailyPricesRepository.findAll().stream()
                .filter(hdp -> hdp.getBill() != null && hdp.getBill().getId().equals(bill.getId()))
                .toList();

        for (HomestayDailyPrice dailyPrice : dailyPrices) {
            dailyPrice.setIsBooked(false);
            homestayDailyPricesRepository.save(dailyPrice);
        }

        // Cập nhật trạng thái bill thành SUCCEED
//        bill.setStatus(StatusBill.SUCCEED);
        billRepository.save(bill);

        log.info("Check-out confirmed for bill {}. Unlocked {} daily prices.", bill.getId(), dailyPrices.size());

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
                    // Đã tồn tại: set isBooked = false (bật lại)
                    HomestayDailyPrice dailyPrice = existingDailyPrice.get();
                    dailyPrice.setIsBooked(false);
                    dailyPrice.setPrice(price);
                    // Xóa bill nếu có (unlock)
                    dailyPrice.setBill(null);
                    homestayDailyPricesRepository.save(dailyPrice);
                } else {
                    // Chưa tồn tại: tạo mới với isBooked = false
                    HomestayDailyPrice newDailyPrice = HomestayDailyPrice.builder()
                            .homestay(homestay)
                            .pricePerDay(pricePerDay)
                            .price(price)
                            .isBooked(false)
                            .bill(null)
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

                // Tìm PricePerDay
                PricePerDay pricePerDay = pricePerDayRepository.findByDay(day).orElse(null);
                if (pricePerDay == null) {
                    // Không có PricePerDay thì không có gì để tắt
                    continue;
                }

                // Tìm HomestayDailyPrice đã tồn tại
                Optional<HomestayDailyPrice> existingDailyPrice = homestayDailyPricesRepository
                        .findOneByHomestayAndPricePerDay(homestay, pricePerDay);

                if (existingDailyPrice.isPresent()) {
                    HomestayDailyPrice dailyPrice = existingDailyPrice.get();
                    
                    // Nếu đã được book (có bill) thì set isBooked = true (không cho book thêm)
                    // Nếu chưa được book thì xóa luôn
                    if (dailyPrice.getBill() != null || Boolean.TRUE.equals(dailyPrice.getIsBooked())) {
                        // Đã được book: set isBooked = true để không cho book thêm
                        dailyPrice.setIsBooked(true);
                        homestayDailyPricesRepository.save(dailyPrice);
                    } else {
                        // Chưa được book: xóa luôn
                        homestayDailyPricesRepository.delete(dailyPrice);
                    }
                }
                // Nếu không tồn tại thì không làm gì (đã tắt rồi)
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
                    if (!Boolean.TRUE.equals(dailyPrice.getIsBooked()) && dailyPrice.getBill() == null) {
                        dailyPrice.setPrice(price);
                        homestayDailyPricesRepository.save(dailyPrice);
                    }
                } else {
                    // Tạo mới HomestayDailyPrice
                    HomestayDailyPrice newDailyPrice = HomestayDailyPrice.builder()
                            .price(price)
                            .isBooked(false)
                            .pricePerDay(pricePerDay)
                            .homestay(homestay)
                            .bill(null)
                            .build();
                    homestayDailyPricesRepository.save(newDailyPrice);
                }
            }

            return new ApiResponse<>(200, "Homestay prices updated successfully", null);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Error updating homestay prices: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> cancelBill(Long hostUserId, Long billId) {
        // Tìm bill
        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + billId, null);
        }

        // Validate: Bill phải thuộc về homestay của host này
        User hostUser = userRepository.findById(hostUserId).orElse(null);
        if (hostUser == null) {
            return new ApiResponse<>(404, "User not found", null);
        }
        Host host = hostRepository.findByUser(hostUser);
        if (host == null) {
            return new ApiResponse<>(404, "Host not found", null);
        }

        if (bill.getHomestay() == null || !Objects.equals(bill.getHomestay().getHost().getId(), host.getId())) {
            return new ApiResponse<>(403, "You can only cancel bills for your own homestays", null);
        }

        // Validate: Bill phải ở trạng thái CHECKIN_PENDING (đã thanh toán cọc nhưng chưa check-in)
        if (bill.getStatus() != StatusBill.REMAINING_PAYMENT_PENDING) {
            return new ApiResponse<>(400, "Bill cannot be cancelled. Current status: " + bill.getStatus(), null);
        }

        // Kiểm tra thời gian: phải sau 3h từ thời gian check-in
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkIn = bill.getCheckIn();

        // Kiểm tra xem đã qua 3h từ thời gian check-in chưa
        LocalDateTime threeHoursAfterCheckIn = checkIn.plusHours(3);
        if (now.isBefore(threeHoursAfterCheckIn)) {
            return new ApiResponse<>(400, "Cannot cancel bill. Must wait 3 hours after check-in time", null);
        }

        // Unlock homestay_daily_prices
        List<HomestayDailyPrice> dailyPrices = homestayDailyPricesRepository.findAll().stream()
                .filter(hdp -> hdp.getBill() != null && hdp.getBill().getId().equals(bill.getId()))
                .toList();

        for (HomestayDailyPrice dailyPrice : dailyPrices) {
            dailyPrice.setIsBooked(false);
            dailyPrice.setBill(null);
            homestayDailyPricesRepository.save(dailyPrice);
        }

//         Cập nhật status bill thành PAYMENT_FAILED (không hoàn tiền)
        bill.setStatus(StatusBill.CHECKIN_EXPIRED);
        billRepository.save(bill);

        return new ApiResponse<>(200, "Bill cancelled successfully. No refund will be processed.", null);
    }

    // Trạng thái bill đã hoàn thành (không cần refund khi ẩn homestay)
    private static final EnumSet<StatusBill> COMPLETED_BILL_STATUSES = EnumSet.of(
            StatusBill.DEPOSIT_PAID,
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
}
