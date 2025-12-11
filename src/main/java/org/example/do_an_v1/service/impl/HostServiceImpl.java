package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.CheckinRequest;
import org.example.do_an_v1.dto.request.CheckoutRequest;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayPriceRequest;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.entity.Admin;
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
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.HomestayDailyPricesRepository;
import org.example.do_an_v1.repository.HomestayRepository;
import org.example.do_an_v1.repository.HostRepository;
import org.example.do_an_v1.repository.PricePerDayRepository;
import org.example.do_an_v1.repository.TransactionRepository;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {

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
    public ApiResponse<PageResponse<List<HostDTO>>> getHostsForAdmin(Long adminUserId, StatusHost status, int page, int size) {
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

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
    public ApiResponse<HostDTO> getHostDetailForAdmin(Long adminUserId, Long hostUserId) {
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

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

        // Lọc các bill đã/đang được sử dụng (logic tương tự history customer)
        List<Bill> filtered = bills.stream()
                .filter(bill -> {
                    StatusBill status = bill.getStatus();
                    return status == StatusBill.SUCCEED
//                            || status == StatusBill.COMPLAINT_EXPIRED
                            || status == StatusBill.CHECKIN_EXPIRED
                            || status == StatusBill.COMPLAINT_PENDING
                            || status == StatusBill.CHECKIN_PENDING;
                })
                .toList();

        List<BillDTO> billDTOS = filtered.stream()
                .map(BillMapper::toDTO)
                .toList();

        return new ApiResponse<>(200, "Bills for host homestays retrieved successfully", billDTOS);
    }

    @Override
    @Transactional
    public ApiResponse<?> confirmCheckin(Long hostUserId, CheckinRequest request) {
        if (hostUserId == null) {
            throw new IllegalArgumentException("Host user ID is required");
        }
        if (request == null || request.getBillId() == null) {
            throw new IllegalArgumentException("Bill ID is required");
        }

        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + request.getBillId()));

        // Validate: Bill phải có homestay
        if (bill.getHomestay() == null) {
            throw new IllegalStateException("Bill must have a homestay associated");
        }

        // Validate: Host phải sở hữu homestay này
        Long homestayHostId = bill.getHomestay().getHost().getId();
        if (!Objects.equals(homestayHostId, hostUserId)) {
            throw new IllegalStateException("Host can only check-in customers for their own homestays");
        }

        // Validate: Bill phải ở trạng thái CHECKIN_PENDING
        if (bill.getStatus() != StatusBill.CHECKIN_PENDING) {
            throw new IllegalStateException("Bill must be in CHECKIN_PENDING status to confirm checkin. Current status: " + bill.getStatus());
        }

        // Tính 70% còn lại cần thanh toán
        if (bill.getTotalAmount() == null) {
            throw new IllegalStateException("Bill total amount is not set");
        }

        // Tính tổng số tiền đã thanh toán (30% cọc)
        double paidAmount = transactionRepository.findByBillId(bill.getId()).stream()
                .filter(t -> t.getTransactionType() == TypeTransaction.BOOKING_PAYMENT 
                        && t.getStatus() == StatusTransaction.SUCCESS)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        // Tính 70% còn lại
        double remainingAmount = bill.getTotalAmount().doubleValue() - paidAmount;

        // Tạo transaction mới cho phần còn lại (70%)
        if (remainingAmount > 0) {
            // Lấy admin user
            User adminUser = adminRepository.findAll().stream()
                    .map(Admin::getUser)
                    .findFirst()
                    .orElse(null);

            if (adminUser == null) {
                throw new IllegalStateException("No admin user found for transaction");
            }

            Transaction remainingPaymentTransaction = Transaction.builder()
                    .completedAt(LocalDateTime.now().plusMinutes(15))
                    .transactionType(TypeTransaction.BOOKING_PAYMENT)
                    .status(StatusTransaction.PENDING)
                    .bill(bill)
                    .fromUser(bill.getCustomer().getUser())
                    .toUser(adminUser)
                    .amount(java.math.BigDecimal.valueOf(remainingAmount))
                    .build();
            transactionRepository.save(remainingPaymentTransaction);
        }

        // Cập nhật trạng thái bill thành REMAINING_PAYMENT_PENDING (cần thanh toán phần còn lại)
        bill.setStatus(StatusBill.REMAINING_PAYMENT_PENDING);
        bill.setActualCheckinTime(LocalDateTime.now());
        billRepository.save(bill);

        return new ApiResponse<>(200, "Check-in confirmed successfully. Please pay the remaining 70% of the bill.", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> confirmCheckout(CheckoutRequest request) {
        if (request == null || request.getBillId() == null) {
            throw new IllegalArgumentException("Bill ID is required");
        }

        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + request.getBillId()));

        // Validate: Bill phải ở trạng thái COMPLAINT_PENDING
        if (bill.getStatus() != StatusBill.COMPLAINT_PENDING) {
            throw new IllegalStateException("Bill must be in COMPLAINT_PENDING status to confirm checkout. Current status: " + bill.getStatus());
        }

        // Validate: Host phải sở hữu homestay này
        // (Có thể thêm validation này nếu cần)

        // Cập nhật trạng thái bill thành SUCCEED
        bill.setStatus(StatusBill.SUCCEED);
        billRepository.save(bill);

        return new ApiResponse<>(200, "Check-out confirmed successfully. Bill status changed to SUCCEED", null);
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
        if (bill.getStatus() != StatusBill.CHECKIN_PENDING) {
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

        // Cập nhật status bill thành PAYMENT_FAILED (không hoàn tiền)
        bill.setStatus(StatusBill.PAYMENT_FAILED);
        billRepository.save(bill);

        return new ApiResponse<>(200, "Bill cancelled successfully. No refund will be processed.", null);
    }
}
