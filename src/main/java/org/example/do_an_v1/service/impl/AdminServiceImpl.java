package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.*;
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
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.*;
import org.example.do_an_v1.exception.ResourceNotFoundException;
import org.example.do_an_v1.mapper.BillMapper;
import org.example.do_an_v1.mapper.ComplaintMapper;
import org.example.do_an_v1.mapper.HomestayMapper;
import org.example.do_an_v1.mapper.HostMapper;
import org.example.do_an_v1.mapper.TransactionMapper;
import org.example.do_an_v1.mapper.UserMapper;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.AdminService;
import org.example.do_an_v1.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jose.JOSEException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final int INVITE_CODE_LENGTH = 6;
    private static final EnumSet<StatusBill> COMPLETED_BILL_STATUSES = EnumSet.of(
            StatusBill.SUCCEED,
            StatusBill.REFUNDED,
            StatusBill.REJECTED,
            StatusBill.CHECKIN_EXPIRED,
            StatusBill.CANCELLED
    );
    private static final List<TypeTransaction> CUSTOMER_REVENUE_TYPES = List.of(
            TypeTransaction.CUSTOMER_PAYMENT_ADMIN,
            TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST,
            TypeTransaction.CUSTOMER_PAYMENT_ADMIN_SECOND,
            TypeTransaction.BOOKING_PAYMENT
    );
    private static final List<TypeTransaction> HOST_PAYOUT_TYPES = List.of(
            TypeTransaction.ADMIN_PAYMENT_HOST
    );
    private static final List<TypeTransaction> CUSTOMER_REFUND_TYPES = List.of(TypeTransaction.REFUND);
    private static final Comparator<Bill> BILL_CREATED_AT_DESC = Comparator
            .comparing(Bill::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .reversed();

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
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
    private final UserMapper userMapper;
    private final SecurityService securityService;

    @Override
    @Transactional
    public ApiResponse<?> login(AdminLoginRequest request) {
        // Validate input
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new ApiResponse<>(400, "Username is required", null);
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return new ApiResponse<>(400, "Password is required", null);
        }

        // Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername().trim());
        if (user == null) {
            return new ApiResponse<>(401, "Invalid username or password", null);
        }

        // Kiểm tra user có phải là admin không
        Admin admin = user.getAdmin();
        if (admin == null) {
            return new ApiResponse<>(401, "Invalid username or password", null);
        }

        // Kiểm tra password
        String providedPassword = request.getPassword().trim();
        String storedPassword = admin.getPassword();
        
        if (storedPassword == null || !storedPassword.equals(providedPassword)) {
            return new ApiResponse<>(401, "Invalid username or password", null);
        }

        // Kiểm tra admin status
        if (admin.getStatus() != Status.ACTIVE) {
            return new ApiResponse<>(403, "Admin account is not active. Current status: " + admin.getStatus(), null);
        }

        // Set isOnline = true
        if (!Boolean.TRUE.equals(user.getIsOnline())) {
            user.setIsOnline(true);
            userRepository.save(user);
        }

        // Tạo roles cho admin
        List<String> roles = new ArrayList<>();
        if (admin.getLevelAdmin() == LevelAdmin.SUPER_ADMIN) {
            roles.add("SUPER_ADMIN");
        } else {
            roles.add("ADMIN");
        }

        // Tạo JWT token
        String token;
        try {
            token = securityService.createTokenSystem(user, roles);
        } catch (JOSEException e) {
            return new ApiResponse<>(500, "Cannot create token: " + e.getMessage(), null);
        }

        // Map user sang UserDTO
        UserDTO userDTO = userMapper.toUserDTO(user, roles, admin.getStatus(), null, null);

        return new ApiResponse<>(200, "Login successful", AccessTokenSystemDTO.builder()
                .token(token)
                .user(userDTO)
                .build());
    }

    @Override
    @Transactional
    public ApiResponse<AdminInvitationResponse> createAdmin( AdminInviteRequest request) throws RuntimeException {
        
        // Kiểm tra username đã tồn tại chưa
        String username = request.getUsername().trim();
        User existingUserByUsername = userRepository.findByUsername(username);
        if (Objects.nonNull(existingUserByUsername)) {
            return new ApiResponse<>(409, "Username đã tồn tại", null);
        }
        
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail);
        if (Objects.isNull(user)) {
            user = User.builder()
                    .email(normalizedEmail)
                    .username(username)
                    .name(request.getFullName())
                    .phone(request.getPhone())
                    .build();
        } else if (Objects.nonNull(user.getAdmin()) && Status.ACTIVE.equals(user.getAdmin().getStatus())) {
            Admin existingAdmin = user.getAdmin();
            AdminInvitationResponse conflictResponse = AdminInvitationResponse.builder()
                    .admin(profileMapper.toAdminDTO(existingAdmin))
                    .status(false)
                    .build();
            return new ApiResponse<>(409,
                    "Admin account already active for email: " + normalizedEmail,
                    conflictResponse);
        } else {
            user.setName(request.getFullName());
            user.setUsername(username);
            if (Objects.nonNull(request.getPhone())) {
                user.setPhone(request.getPhone());
            }
        }

        user = userRepository.save(user);

        Admin admin = adminRepository.findById(user.getId()).orElse(null);
        if (Objects.isNull(admin)) {
            admin = Admin.builder()
                    .user(user)
                    .status(Status.ACTIVE)
                    .password(request.getPassword())
                    .levelAdmin(Objects.requireNonNullElse(request.getLevelAdmin(), LevelAdmin.ADMIN))
                    .build();
        } else {
            admin.setStatus(Status.ACTIVE);
            admin.setLevelAdmin(Objects.requireNonNullElse(request.getLevelAdmin(), admin.getLevelAdmin()));
        }
        admin.setRole(RoleUser.ADMIN);
        admin = adminRepository.save(admin);

//        String inviteCode = generateInviteCode();
//        // expired_at is derived here so invitation validity stays under server control, never taken from client input
//        confirmEmailRepository.save(ConfirmEmail.builder()
//                .email(normalizedEmail)
//                .code(inviteCode)
//                .expired_at(LocalDateTime.now().plusHours(24))
//                .build());
//
//        emailService.sendSimpleEmail(normalizedEmail, buildInvitationMessage(inviteCode));

        AdminInvitationResponse response = AdminInvitationResponse.builder()
                .admin(profileMapper.toAdminDTO(admin))
                .status(true)
                .build();

        return new ApiResponse<>(200,
                "Invitation sent successfully. Activation code is returned for testing purposes only.",
                response);
    }

    @Override
    @Transactional
    public ApiResponse<AdminDTO> updateStatusAdmin(UpdateStatusAdminRequest request) throws RuntimeException {
        Admin admin = adminRepository.findById(request.getIdAdmin())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + request.getIdAdmin()));

        admin.setStatus(request.getStatus());
        admin = adminRepository.save(admin);

        return new ApiResponse<>(200, "Admin status updated successfully", profileMapper.toAdminDTO(admin));
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

        // Nếu chuyển sang BAN hoặc INACTIVE, kiểm tra và xử lý các bills liên quan
        if (status == StatusHomestay.BAN || status == StatusHomestay.INACTIVE) {
            List<Bill> bills = billRepository.findByHomestay(homestay);
            LocalDateTime now = LocalDateTime.now();
            
            // Lọc các bills có checkIn trong tương lai và status = REMAINING_PAYMENT_PENDING
            List<Bill> affectedBills = bills.stream()
                    .filter(bill -> bill.getCheckIn() != null && bill.getCheckIn().isAfter(now))
                    .filter(bill -> bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING)
                    .toList();

            if (!affectedBills.isEmpty()) {
                // Lấy admin user để tạo transaction REFUND
                User adminUser = adminRepository.findAll().stream()
                        .filter(admin -> admin.getStatus() == Status.ACTIVE)
                        .map(Admin::getUser)
                        .findFirst()
                        .orElse(null);

                if (adminUser == null) {
                    return new ApiResponse<>(500, "No active admin found to process refunds", null);
                }

                // Xử lý từng bill bị ảnh hưởng
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
                                    .fromUser(adminUser)
                                    .toUser(bill.getCustomer().getUser())
                                    .bill(bill)
                                    .build();
                            transactionRepository.save(refundTransaction);
                        }
                    }
                }
            }
        }

        homestay.setStatusHomestay(status);
        Homestay savedHomestay = homestayRepository.save(homestay);

        List<HomestayImage> images = homestayImageRepository.findByHomestay(savedHomestay);
        HomestayDTO response = homestayMapper.toDto(savedHomestay, images);

        return new ApiResponse<>(200, "Homestay status updated successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<AdminDTO>>> getAllAdmins(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        
        Page<Admin> adminPage = adminRepository.findAll(pageable);
        
        List<AdminDTO> admins = adminPage.getContent().stream()
                .map(profileMapper::toAdminDTO)
                .toList();
        
        PageResponse<List<AdminDTO>> response = PageResponse.<List<AdminDTO>>builder()
                .page(adminPage.getNumber())
                .size(adminPage.getSize())
                .total(adminPage.getTotalElements())
                .items(admins)
                .build();
        
        return new ApiResponse<>(200, "Admins retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<CustomerDTO>>> getAllCustomers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        
        List<CustomerDTO> customers = customerPage.getContent().stream()
                .map(profileMapper::toCustomerDTO)
                .toList();
        
        PageResponse<List<CustomerDTO>> response = PageResponse.<List<CustomerDTO>>builder()
                .page(customerPage.getNumber())
                .size(customerPage.getSize())
                .total(customerPage.getTotalElements())
                .items(customers)
                .build();
        
        return new ApiResponse<>(200, "Customers retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<AdminDTO> updateAdminStatus(Long idAdmin, Status status) {
        if (idAdmin == null) {
            throw new IllegalArgumentException("Admin id is required");
        }
        if (status == null) {
            return new ApiResponse<>(400, "Status is required", null);
        }

        Admin admin = adminRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found for id " + idAdmin));

        admin.setStatus(status);
        Admin savedAdmin = adminRepository.save(admin);

        return new ApiResponse<>(200, "Admin status updated successfully", profileMapper.toAdminDTO(savedAdmin));
    }

    @Override
    @Transactional
    public ApiResponse<CustomerDTO> updateCustomerStatus(Long idCustomer, Status status) {
        if (idCustomer == null) {
            throw new IllegalArgumentException("Customer id is required");
        }
        if (status == null) {
            return new ApiResponse<>(400, "Status is required", null);
        }

        Customer customer = customerRepository.findById(idCustomer)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found for id " + idCustomer));

        customer.setStatus(status);
        Customer savedCustomer = customerRepository.save(customer);

        return new ApiResponse<>(200, "Customer status updated successfully", profileMapper.toCustomerDTO(savedCustomer));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AdminDTO> getAdminById(Long idAdmin) {
        if (idAdmin == null) {
            throw new IllegalArgumentException("Admin id is required");
        }

        Admin admin = adminRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found for id " + idAdmin));

        return new ApiResponse<>(200, "Admin detail retrieved successfully", profileMapper.toAdminDTO(admin));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerDTO> getCustomerById(Long idCustomer) {
        if (idCustomer == null) {
            throw new IllegalArgumentException("Customer id is required");
        }

        Customer customer = customerRepository.findById(idCustomer)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found for id " + idCustomer));

        return new ApiResponse<>(200, "Customer detail retrieved successfully", profileMapper.toCustomerDTO(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<BillDTO>>> getAllBills(
            int page,
            int size,
            StatusBill status,
            Long customerId,
            Long hostId,
            Long homestayId
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;

        List<Bill> filteredBills = billRepository.findAll().stream()
                .filter(bill -> status == null || bill.getStatus() == status)
                .filter(bill -> customerId == null
                        || (bill.getCustomer() != null && Objects.equals(bill.getCustomer().getId(), customerId)))
                .filter(bill -> homestayId == null
                        || (bill.getHomestay() != null && Objects.equals(bill.getHomestay().getId(), homestayId)))
                .filter(bill -> hostId == null
                        || (bill.getHomestay() != null
                        && bill.getHomestay().getHost() != null
                        && Objects.equals(bill.getHomestay().getHost().getId(), hostId)))
                .sorted(BILL_CREATED_AT_DESC)
                .collect(Collectors.toList());

        long total = filteredBills.size();
        int fromIndex = Math.min(safePage * safeSize, filteredBills.size());
        int toIndex = Math.min(fromIndex + safeSize, filteredBills.size());

        List<BillDTO> billDTOS = filteredBills.subList(fromIndex, toIndex).stream()
                .map(BillMapper::toDTO)
                .collect(Collectors.toList());

        PageResponse<List<BillDTO>> response = PageResponse.<List<BillDTO>>builder()
                .page(safePage)
                .size(safeSize)
                .total(total)
                .items(billDTOS)
                .build();

        return new ApiResponse<>(200, "Bills retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TransactionDTO>> getTransactionsForBill(Long billId) {
        if (billId == null) {
            throw new IllegalArgumentException("Bill id is required");
        }

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found for id " + billId));

        List<Transaction> transactions = transactionRepository.findByBill(bill);

        List<TransactionDTO> transactionDTOS = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Transactions retrieved successfully", transactionDTOS);
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
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Pending refunds retrieved successfully", refundDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TransactionDTO>> getHostPayoutTransactions(StatusTransaction status) {
        // Lấy tất cả transactions và filter theo type và status
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> HOST_PAYOUT_TYPES.contains(t.getTransactionType()))
                .filter(t -> status == null || t.getStatus() == status)
                .sorted(Comparator.comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());

        // Map sang DTO
        List<TransactionDTO> transactionDTOs = filteredTransactions.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Host payout transactions retrieved successfully", transactionDTOs);
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

        // Lấy bill từ transaction để lấy customer
        Bill bill = transaction.getBill();
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found for this transaction", null);
        }

        Customer customer = bill.getCustomer();
        if (customer == null) {
            return new ApiResponse<>(404, "Customer not found for this bill", null);
        }

        User customerUser = customer.getUser();
        if (customerUser == null) {
            return new ApiResponse<>(404, "Customer user not found", null);
        }

        // Lấy admin user
        User adminUser = admin.getUser();
        if (adminUser == null) {
            return new ApiResponse<>(500, "Admin user not found", null);
        }

        // Cập nhật transaction: thêm proof image, set fromUser/toUser và chuyển status sang SUCCESS
        transaction.setProofImageUrl(request.getProofImageUrl());
        transaction.setFromUser(adminUser); // Admin là người gửi (hoàn tiền)
        transaction.setToUser(customerUser); // Customer là người nhận (nhận tiền hoàn lại)
        transaction.setStatus(StatusTransaction.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return new ApiResponse<>(200, "Refund confirmed successfully", TransactionMapper.toDTO(transaction));
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
            bill.setStatus(StatusBill.REFUNDED_PENDING);
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

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AdminFinanceReportResponse> getFinanceReport() {
        List<Bill> bills = billRepository.findAll();
        long totalBills = bills.size();
        long completedBills = bills.stream()
                .filter(bill -> bill.getStatus() != null && COMPLETED_BILL_STATUSES.contains(bill.getStatus()))
                .count();

        List<Transaction> transactions = transactionRepository.findAll();

        BigDecimal totalRevenueFromCustomers = calculateTotalAmount(transactions, CUSTOMER_REVENUE_TYPES);
        BigDecimal totalPayoutToHosts = calculateTotalAmount(transactions, HOST_PAYOUT_TYPES);
        BigDecimal totalRefundsToCustomers = calculateTotalAmount(transactions, CUSTOMER_REFUND_TYPES);

        BigDecimal netRevenue = totalRevenueFromCustomers
                .subtract(totalPayoutToHosts)
                .subtract(totalRefundsToCustomers);

        AdminFinanceReportResponse report = AdminFinanceReportResponse.builder()
                .totalBills(totalBills)
                .completedBills(completedBills)
                .totalRevenueFromCustomers(totalRevenueFromCustomers)
                .totalPayoutToHosts(totalPayoutToHosts)
                .totalRefundsToCustomers(totalRefundsToCustomers)
                .netRevenue(netRevenue)
                .build();

        return new ApiResponse<>(200, "Finance report generated successfully", report);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<HomestayStatisticsDTO>> getAllHomestayStatistics() {
        // Lấy tất cả homestays
        List<Homestay> allHomestays = homestayRepository.findAll();

        List<HomestayStatisticsDTO> statisticsList = allHomestays.stream()
                .map(this::calculateHomestayStatistics)
                .toList();

        return new ApiResponse<>(200, "All homestay statistics retrieved successfully", statisticsList);
    }

    /**
     * Tính thống kê cho một homestay
     */
    private HomestayStatisticsDTO calculateHomestayStatistics(Homestay homestay) {
        // Đếm số lượng bills (bookings)
        long totalBookings = billRepository.countByHomestay(homestay);

        // Tính tổng tiền kiếm được từ các Transaction PAYLOAD_HOST thành công
        // Lấy tất cả bills của homestay
        List<Bill> bills = billRepository.findByHomestay(homestay);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Bill bill : bills) {

            if(bill.getStatus() == StatusBill.SUCCEED
                    || bill.getStatus() == StatusBill.REJECTED
                    || bill.getStatus() == StatusBill.CANCELLED
                    || bill.getStatus() == StatusBill.CHECKIN_EXPIRED
                    || bill.getStatus() == StatusBill.REMAINING_PAYMENT_FAILED
            ){
                if(bill.getTotalAmount() != null) totalRevenue = totalRevenue.add(bill.getTotalAmount());
            }
            // Tìm các transaction PAYLOAD_HOST thành công của bill này
//            List<Transaction> payloadTransactions = transactionRepository.findByBill(bill).stream()
//                    .filter(t -> t.getTransactionType() == TypeTransaction.PAYLOAD_HOST)
//                    .filter(t -> t.getStatus() == StatusTransaction.SUCCESS)
//                    .toList();
//
//            for (Transaction transaction : payloadTransactions) {
//                if (transaction.getAmount() != null) {
//                    totalRevenue = totalRevenue.add(transaction.getAmount());
//                }
//            }
        }

        // Đếm số lượng complaints
        long totalComplaints = complaintRepository.countByHomestay(homestay);

        return HomestayStatisticsDTO.builder()
                .homestayId(homestay.getId())
                .homestayTitle(homestay.getTitle())
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .totalComplaints(totalComplaints)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<ComplaintDTO>>> getAllComplaints(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Complaint> complaintPage = complaintRepository.findAll(pageable);

        // Map complaints với transaction REFUND liên quan đến complaint
        List<ComplaintDTO> complaints = complaintPage.getContent().stream()
                .map(complaint -> {
                    // Chỉ lấy transaction REFUND của bill liên quan đến complaint này
                    Bill bill = complaint.getBill();
                    Transaction refundTransaction = null;
                    if (bill != null) {
                        // Lấy tất cả transactions của bill
                        List<Transaction> allTransactions = transactionRepository.findByBill(bill);
                        // Lọc chỉ lấy transaction REFUND và lấy transaction mới nhất (nếu có nhiều)
                        refundTransaction = allTransactions.stream()
                                .filter(t -> t.getTransactionType() == TypeTransaction.REFUND)
                                .max(Comparator.comparing(
                                        Transaction::getCreatedAt,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                ))
                                .orElse(null);
                    }
                    // Map complaint với transaction REFUND (chỉ 1 transaction)
                    return ComplaintMapper.toDTO(complaint, refundTransaction);
                })
                .toList();

        PageResponse<List<ComplaintDTO>> response = PageResponse.<List<ComplaintDTO>>builder()
                .page(complaintPage.getNumber())
                .size(complaintPage.getSize())
                .total(complaintPage.getTotalElements())
                .items(complaints)
                .build();

        return new ApiResponse<>(200, "Complaints retrieved successfully", response);
    }

    private BigDecimal calculateTotalAmount(List<Transaction> transactions, Collection<TypeTransaction> transactionTypes) {
        if (transactions == null || transactions.isEmpty() || transactionTypes == null || transactionTypes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return transactions.stream()
                .filter(transaction -> transaction.getStatus() == StatusTransaction.SUCCESS)
                .filter(transaction -> transaction.getTransactionType() != null
                        && transactionTypes.contains(transaction.getTransactionType()))
                .filter(transaction -> transaction.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<HostWithPendingPayoutTransactionsResponse>> getHostsWithPendingPayoutTransactions() {
        // Lấy tất cả transactions có type là HOST_PAYOUT_TYPES và status là PENDING
        // Sử dụng JOIN FETCH để eager load Bill, Homestay, Host, Address trong một query (tránh N+1)
        List<Transaction> pendingPayoutTransactions = transactionRepository.findByTransactionTypeInAndStatusWithJoins(
                HOST_PAYOUT_TYPES,
                StatusTransaction.PENDING
        );

        if (pendingPayoutTransactions.isEmpty()) {
            return new ApiResponse<>(200, "No pending payout transactions found", new ArrayList<>());
        }

        // Group transactions theo Host (thông qua Bill -> Homestay -> Host)
        java.util.Map<Host, java.util.Map<Homestay, List<Transaction>>> hostHomestayTransactionsMap = 
                pendingPayoutTransactions.stream()
                        .filter(t -> t.getBill() != null 
                                && t.getBill().getHomestay() != null 
                                && t.getBill().getHomestay().getHost() != null)
                        .collect(Collectors.groupingBy(
                                t -> t.getBill().getHomestay().getHost(),
                                Collectors.groupingBy(
                                        t -> t.getBill().getHomestay()
                                )
                        ));

        // Tối ưu: Batch load tất cả images cho tất cả homestays cùng lúc (tránh N+1 query)
        List<Homestay> allHomestays = hostHomestayTransactionsMap.values().stream()
                .flatMap(homestayMap -> homestayMap.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
        
        // Load tất cả images trong một query
        List<HomestayImage> allImages = allHomestays.isEmpty() 
                ? new ArrayList<>() 
                : homestayImageRepository.findByHomestayIn(allHomestays);
        
        // Group images theo homestay để dễ lookup
        java.util.Map<Long, List<HomestayImage>> imagesByHomestayId = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getHomestay().getId()));

        // Map sang DTO
        List<HostWithPendingPayoutTransactionsResponse> result = hostHomestayTransactionsMap.entrySet().stream()
                .map(entry -> {
                    Host host = entry.getKey();
                    java.util.Map<Homestay, List<Transaction>> homestayTransactionsMap = entry.getValue();

                    // Map Host sang HostDTO
                    HostDTO hostDTO = HostMapper.hostMapHostDTO(host);

                    // Map Homestay và Transactions
                    List<HostWithPendingPayoutTransactionsResponse.HomestayWithTransactionsDTO> homestayWithTransactionsList =
                            homestayTransactionsMap.entrySet().stream()
                                    .map(homestayEntry -> {
                                        Homestay homestay = homestayEntry.getKey();
                                        List<Transaction> transactions = homestayEntry.getValue();

                                        // Tối ưu: Sử dụng HomestaySummaryDTO thay vì HomestayDTO để giảm dữ liệu
                                        // Chỉ load thông tin cần thiết, không load facilities, amenities, rules, dailyPrices, personCapacities
                                        HomestaySummaryDTO homestaySummary = homestayMapper.toSummary(homestay);
                                        
                                        // Convert HomestaySummaryDTO sang HomestayDTO đơn giản (chỉ thông tin cơ bản)
                                        HomestayDTO homestayDTO = HomestayDTO.builder()
                                                .id(homestaySummary.getId())
                                                .hostId(homestaySummary.getHostId())
                                                .title(homestaySummary.getTitle())
                                                .category(homestaySummary.getCategory())
                                                .status(homestaySummary.getStatus())
                                                .address(homestay.getAddress() != null ? HomestayDTO.AddressDTO.builder()
                                                        .addressLine(homestay.getAddress().getAddressLine())
                                                        .city(homestay.getAddress().getCity())
                                                        .state(homestay.getAddress().getState())
                                                        .latitude(homestay.getAddress().getLatitude())
                                                        .longitude(homestay.getAddress().getLongitude())
                                                        .build() : null)
                                                // Chỉ load images (đã được batch load)
                                                .images(imagesByHomestayId.getOrDefault(homestay.getId(), new ArrayList<>())
                                                        .stream()
                                                        .map(img -> HomestayDTO.HomestayImageDTO.builder()
                                                                .id(img.getId())
                                                                .imageUrl(img.getImageUrl())
                                                                .primary(img.getIsPrimary())
                                                                .build())
                                                        .collect(Collectors.toList()))
                                                // Không load các dữ liệu không cần thiết
                                                .facilities(null)
                                                .amenities(null)
                                                .rules(null)
                                                .dailyPrices(null)
                                                .personCapacities(null)
                                                .description(null)
                                                .rating(null)
                                                .numbersOfReview(null)
                                                .minGuest(null)
                                                .maxGuest(null)
                                                .numBedrooms(null)
                                                .numBeds(null)
                                                .numBathrooms(null)
                                                .numKitchen(null)
                                                .advancedPayment(null)
                                                .warningCount(null)
                                                .basePrice(null)
                                                .build();

                                        // Map Transactions sang TransactionDTO
                                        List<TransactionDTO> transactionDTOs = transactions.stream()
                                                .map(TransactionMapper::toDTO)
                                                .collect(Collectors.toList());

                                        return HostWithPendingPayoutTransactionsResponse.HomestayWithTransactionsDTO.builder()
                                                .homestay(homestayDTO)
                                                .transactions(transactionDTOs)
                                                .build();
                                    })
                                    .collect(Collectors.toList());

                    return HostWithPendingPayoutTransactionsResponse.builder()
                            .host(hostDTO)
                            .homestays(homestayWithTransactionsList)
                            .build();
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Hosts with pending payout transactions retrieved successfully", result);
    }
}
