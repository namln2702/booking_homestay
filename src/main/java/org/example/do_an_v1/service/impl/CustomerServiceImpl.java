package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.configuration.SessionConfig;
import org.example.do_an_v1.dto.*;
import org.example.do_an_v1.dto.request.CancelComplaintRequest;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.*;
import org.example.do_an_v1.mapper.*;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.dto.response.CustomerComplaintResponse;
import org.example.do_an_v1.dto.response.CustomerOrderActionPermissionResponse;
import org.example.do_an_v1.dto.response.CustomerOrderComplaintStatusResponse;
import org.example.do_an_v1.dto.response.CustomerOrderDailyPriceResponse;
import org.example.do_an_v1.dto.response.CustomerOrderDetailResponse;
import org.example.do_an_v1.dto.response.CustomerOrderPaymentStatusResponse;
import org.example.do_an_v1.dto.response.CustomerOrderResponse;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.CustomerService;
import org.example.do_an_v1.service.EmailService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
//import org.example.do_an_v1.utils.Date;
import org.example.do_an_v1.utils.GenNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ProfileMapper profileMapper;
    private final UserRegistrationSupport userRegistrationSupport;
    private final CustomerBookingInfoRepository customerBookingInfoRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;
    private final BillRepository billRepository;
    private final HomestayRepository homestayRepository;
    private final PreferenceRepository preferenceRepository;
    private final SessionConfig sessionConfig;
    private final ImageRepository imageRepository;
    private final ComplaintRepository complaintRepository;
    private final TransactionRepository transactionRepository;
    private final PricePerDayRepository pricePerDayRepository;
    private final EmailService emailService;
    private final AdminRepository adminRepository;

    @Override
    @Transactional
    public ApiResponse<CustomerDTO> upsertCustomerProfile(CustomerDTO dto) throws RuntimeException {
        if (dto == null || dto.getIdUser() == null) {
            throw new IllegalArgumentException("Customer DTO must include idUser");
        }

        User user = userRegistrationSupport.getUserOrThrow(dto.getIdUser());

        Customer customer = customerRepository.findById(user.getId()).orElse(null);
        boolean isNew = false;

        if (customer == null) {
            customer = Customer.builder()
                    .user(user)
                    .role(RoleUser.CUSTOMER)
                    .build();
            isNew = true;
        }

        boolean hasChanges = isNew;

        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                dto.getIdUser(),
                dto.getUsername(),
                dto.getName(),
                dto.getPhone(),
                dto.getAge(),
                dto.getAvatarUrl()
        );

        if (userRegistrationSupport.applyUserAttributes(user, userRequest)) {
            hasChanges = true;
        }

        if (dto.getStatus() != null && !Objects.equals(dto.getStatus(), customer.getStatus())) {
            customer.setStatus(dto.getStatus());
            hasChanges = true;
        }

        if (dto.getDateOfBirth() != null && !Objects.equals(dto.getDateOfBirth(), customer.getDateOfBirth())) {
            customer.setDateOfBirth(dto.getDateOfBirth());
            hasChanges = true;
        }

        if (dto.getQrCodeUrl() != null && !Objects.equals(dto.getQrCodeUrl(), customer.getQrCodeUrl())) {
            customer.setQrCodeUrl(dto.getQrCodeUrl());
            hasChanges = true;
        }

        // lastBooking will be maintained by booking workflows; ignore incoming value for now

        if (customer.getRole() != RoleUser.CUSTOMER) {
            customer.setRole(RoleUser.CUSTOMER);
            hasChanges = true;
        }

        if (!hasChanges) {
            return new ApiResponse<>(200, "Customer information already up to date", profileMapper.toCustomerDTO(customer));
        }

        // createdAt/updatedAt live on BaseEntity and are populated through auditing, never via the request payload
        Customer savedCustomer = customerRepository.save(customer);
        String message = isNew ? "Customer profile created successfully" : "Customer information updated successfully";

        return new ApiResponse<>(200, message, profileMapper.toCustomerDTO(savedCustomer));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerDTO> getCustomerByUserId(Long userId) {
        Customer customer = customerRepository.findById(userId).orElse(null);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found", null);
        }
        return new ApiResponse<>(200, "Customer profile retrieved successfully", profileMapper.toCustomerDTO(customer));
    }

    @Override
    @Transactional
    public ApiResponse<?> booking(Long userId, BookingDTO bookingDTO) {

        Homestay homestay = homestayRepository.findById(bookingDTO.getHomestayId()).orElse(null);
        if (homestay == null) {
            return new ApiResponse<>(404, "Homestay not exists", null);
        }


        // Convert Date sang LocalDate để xử lý
        Date checkInDate = bookingDTO.getCheckIn();
        Date checkOutDate = bookingDTO.getCheckOut();
        
        // Convert Date sang LocalDate
        LocalDate checkInLocalDate = checkInDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate checkOutLocalDate = checkOutDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();


        // Convert sang java.sql.Date để query
        java.sql.Date startDate = java.sql.Date.valueOf(checkInLocalDate);
        java.sql.Date endDate = java.sql.Date.valueOf(checkOutLocalDate);
        
        // Convert Date sang LocalDateTime cho Bill entity
        LocalDateTime checkInDateTime = checkInDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime checkOutDateTime = checkOutDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Check homestay availability
        if(Boolean.TRUE.equals(homestayDailyPricesRepository.checkHomestayAvailability(homestay.getId(), startDate, endDate)))
            return new ApiResponse<>(422, "Room has been booked", null );

        // Lấy User từ userId
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "User not found with id: " + userId, null);
        }

        // Lấy Customer từ User
        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for user id: " + userId, null);
        }

        // Save bill - tạo Bill entity từ BookingDTO
        Bill bill = new Bill();
        bill.setCheckIn(checkInDateTime);
        bill.setCheckOut(checkOutDateTime);
        bill.setActualCheckinTime(null); // actualCheckin sẽ được set khi check-in thực tế
        bill.setHomestay(homestay);
        bill.setCustomer(customer);
        bill.setCode(GenNumber.secureRandomNumbers());
        bill.setStatus(StatusBill.DEPOSIT_PENDING);

        Bill billResult = billRepository.save(bill);

        // Validation đã được xử lý ở Controller layer bằng @Valid
        // Lấy danh sách HomestayDailyPrice để khóa
        List<HomestayDailyPrice> finalDailyPricesToLock = new ArrayList<>();
        
        // Xử lý từng pricePerDay trong danh sách
        // Validation đã được xử lý ở Controller layer bằng @Valid
        for (org.example.do_an_v1.dto.request.PricePerDayRequest pricePerDayRequest : bookingDTO.getPricePerDays()) {
            Date day = pricePerDayRequest.getDay();
            Float price = pricePerDayRequest.getPrice();

            // Check xem ngày đó đã được tạo trong bảng pricePerDay chưa
            PricePerDay pricePerDay = pricePerDayRepository.findByDay(day)
                    .orElse(null);

            if (pricePerDay == null) {
                // Chưa có thì tạo mới PricePerDay với ngày và giá
                pricePerDay = PricePerDay.builder()
                        .day(day)
                        .price(price)
                        .build();
                pricePerDay = pricePerDayRepository.save(pricePerDay);
            } else {
                // Đã có thì lấy ra (có thể cập nhật giá nếu cần)
                // Nếu giá khác nhau, có thể cập nhật hoặc giữ nguyên giá cũ
                // Ở đây ta giữ nguyên giá đã có trong database
               pricePerDay.setPrice(pricePerDayRequest.getPrice());
                pricePerDay = pricePerDayRepository.save(pricePerDay);
            }

            // Tìm hoặc tạo HomestayDailyPrice cho homestay và pricePerDay này
            HomestayDailyPrice homestayDailyPrice = homestayDailyPricesRepository
                    .findOneByHomestayAndPricePerDay(homestay, pricePerDay)
                    .orElse(null);

            if (homestayDailyPrice == null) {
                // Chưa có thì tạo mới HomestayDailyPrice
                homestayDailyPrice = HomestayDailyPrice.builder()
                        .price(pricePerDay.getPrice())
                        .isBooked(Boolean.FALSE)
                        .pricePerDay(pricePerDay)
                        .homestay(homestay)
                        .build();
                homestayDailyPrice = homestayDailyPricesRepository.save(homestayDailyPrice);
            } else {
                // Đã có thì kiểm tra xem đã được booked chưa
                if (Boolean.TRUE.equals(homestayDailyPrice.getIsBooked())) {
                    return new ApiResponse<>(409, "Date " + day + " is already booked", null);
                }
            }

            finalDailyPricesToLock.add(homestayDailyPrice);
        }

        // Khóa các daily prices (set isBooked = true và gán bill)
        Bill finalBillResult = billResult;
        for (HomestayDailyPrice dailyPrice : finalDailyPricesToLock) {
            if (Boolean.TRUE.equals(dailyPrice.getIsBooked())) {
                return new ApiResponse<>(409, "Some dates are already booked", null);
            }
            dailyPrice.setIsBooked(true);
            dailyPrice.setBill(finalBillResult);
            homestayDailyPricesRepository.save(dailyPrice);
        }


        // luu thong tin CustomerBookingInfo neu la nguoi moi
        if(!Objects.isNull(bookingDTO.getCustomerBookingInfoDTO())){
            CustomerBookingInfo customerBookingInfo = CustomerBookingInfoMapper.toEntity(bookingDTO.getCustomerBookingInfoDTO());
            customerBookingInfo = customerBookingInfoRepository.save(customerBookingInfo);
            billResult.setCustomerBookingInfo(customerBookingInfo);
        }


        // Lấy admin user (giả sử có một admin mặc định hoặc lấy từ config)
        // Tạm thời để null, sẽ cần xử lý sau
        User adminUser = userRepository.findAll().stream()
                .filter(u -> u.getAdmin() != null)
                .findFirst()
                .orElse(null);

        if (adminUser == null) {
            return new ApiResponse<>(500, "No admin user found for transaction", null);
        }


        // Tính tổng giá trị bill (100%)
        double totalAmount = finalDailyPricesToLock.stream()
                .mapToDouble(HomestayDailyPrice::getPrice)
                .sum();
        
        // Lưu tổng giá trị vào bill
        billResult.setTotalAmount(java.math.BigDecimal.valueOf(totalAmount));
        
        // Tính 30% cho thanh toán cọc
        double depositAmount = totalAmount * 0.3;
        
        Set<Transaction> transactions = new HashSet<>();
        // Create transaction cho thanh toán cọc (30%)
        Transaction transaction = Transaction.builder()
                .completedAt(LocalDateTime.now().plusMinutes(15))
                .transactionType(TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST)
                .status(StatusTransaction.PENDING)
                .bill(billResult)
                .fromUser(customer.getUser())
                .toUser(adminUser)
                .amount(java.math.BigDecimal.valueOf(depositAmount))
                .build();
        
        transactions.add(transactionRepository.save(transaction));


        // Create code
//        bill.setCode(GenNumber.generate());
//        bill.setStatus(StatusBill.PAYMENT_PENDING);
        billResult.setListTransaction(transactions);
        billResult = billRepository.save(billResult);

        // Reload bill với đầy đủ thông tin để map sang DTO
        Bill billWithDetails = billRepository.findById(billResult.getId()).orElse(null);
        if (billWithDetails == null) {
            return new ApiResponse<>(500, "Bill not found after save", null);
        }

        // Convert sang BillDTO để trả về
        BillDTO billDTO = BillMapper.toDTO(billWithDetails);


        return new ApiResponse<>(200, "Save bill success", billDTO);
    }

    public ApiResponse<?> payment(PaymentDTO paymentDTO){
        return null;
    }

    @Override
    @Transactional
    public ApiResponse<CustomerDTO> updatePreferencesCustomer(Long userId, CustomerDTO customerDTO) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (customerDTO == null) {
            throw new IllegalArgumentException("Customer DTO is required");
        }
        if (customerDTO.getListPreference() == null || customerDTO.getListPreference().isEmpty()) {
            throw new IllegalArgumentException("List of preferences is required");
        }

        // Lấy User theo userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id: " + userId));

        // Tìm Customer theo User
        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        // Tạo Set mới chứa các preferences từ request
        Set<Preference> newPreferences = new HashSet<>();
        for (Long preferenceId : customerDTO.getListPreference()) {
            if (preferenceId == null) {
                continue; // Bỏ qua null values
            }
            Preference preference = preferenceRepository.findById(preferenceId).orElse(null);
            if (preference == null) {
                return new ApiResponse<>(404, "Preference not found with id: " + preferenceId, null);
            }
            newPreferences.add(preference);
        }

        // Cập nhật preferences cho customer (thay thế toàn bộ)
        customer.setListPreferences(newPreferences);
        customer.setStatus(Status.ACTIVE);
        
        // Lưu customer
        Customer savedCustomer = customerRepository.save(customer);
        
        // Map sang DTO để trả về
        CustomerDTO responseDTO = profileMapper.toCustomerDTO(savedCustomer);
        
        return new ApiResponse<>(200, "Customer preferences updated successfully", responseDTO);
    }


    @Override
    @Transactional
    public ApiResponse<?> reviewHomestay(Long userId, ReviewDTO reviewDTO) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (reviewDTO == null) {
            throw new IllegalArgumentException("Review data is required");
        }
        if (reviewDTO.getHomestayId() == null) {
            throw new IllegalArgumentException("Homestay id is required");
        }
        if (reviewDTO.getRating() == null || reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (reviewDTO.getComment() == null || reviewDTO.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment is required");
        }

        // Lấy user và customer
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));

        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        // Lấy homestay
        Homestay homestay = homestayRepository.findById(reviewDTO.getHomestayId())
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + reviewDTO.getHomestayId()));

        // Validate: Customer đã booking homestay này chưa?
        Optional<Bill> billOptional = billRepository.findByHomestayAndCustomer(homestay, customer);
        if (billOptional.isEmpty()) {
            return new ApiResponse<>(403, "You must book this homestay before reviewing", null);
        }

        Bill bill = billOptional.get();
        // Chỉ cho phép review nếu đã check-in hoặc đã hoàn tất
        StatusBill billStatus = bill.getStatus();
        if (billStatus != StatusBill.COMPLAINT_PENDING
                && billStatus != StatusBill.REMAINING_PAYMENT_PENDING
//                && billStatus != StatusBill.COMPLAINT_EXPIRED
                && billStatus != StatusBill.SUCCEED) {
            return new ApiResponse<>(403, "You can only review homestays you have stayed at", null);
        }

        // Validate: Customer đã review homestay này chưa? (tránh duplicate)
        Optional<Review> existingReview = reviewRepository.findByHomestayAndCustomer(homestay, customer);
        if (existingReview.isPresent()) {
            return new ApiResponse<>(409, "You have already reviewed this homestay", null);
        }

        // Tạo Review entity
        Review review = Review.builder()
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment().trim())
                .homestay(homestay)
                .customer(customer)
                .build();

        // Lưu review trước để có ID
        Review savedReview = reviewRepository.save(review);

        // Xử lý images nếu có
        Set<Image> reviewImages = new HashSet<>();
        if (reviewDTO.getImageUrls() != null && !reviewDTO.getImageUrls().isEmpty()) {
            final Review reviewForImages = savedReview; // Make final for lambda
            List<Image> imagesToSave = reviewDTO.getImageUrls().stream()
                    .filter(img -> img != null && img.getImage_url() != null && !img.getImage_url().trim().isEmpty())
                    .map(img -> {
                        Image image = ImageMapper.toEntity(img);
                        image.setReview(reviewForImages);
                        return imageRepository.save(image);
                    })
                    .collect(Collectors.toList());
            reviewImages.addAll(imagesToSave);
            savedReview.setListImage(reviewImages);
            savedReview = reviewRepository.save(savedReview);
        }

        // Sử dụng savedReview làm finalReview
        final Review finalReview = savedReview;

        // Thêm review vào homestay
        Set<Review> reviews = homestay.getListReview();
        if (reviews == null) {
            reviews = new HashSet<>();
        }
        reviews.add(finalReview);
        homestay.setListReview(reviews);

        // Tính lại rating trung bình của homestay
        List<Review> allReviews = reviewRepository.findByHomestay(homestay);
        if (!allReviews.isEmpty()) {
            double averageRating = allReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            homestay.setRating((float) averageRating);
        }

        // Lưu homestay với rating mới
        homestay = homestayRepository.save(homestay);

        // Map sang DTO để trả về
        ReviewDTO responseDTO = ReviewMapper.toDTO(finalReview);

        return new ApiResponse<>(201, "Review submitted successfully", responseDTO);
    }

    @Override
    @Transactional
    public ApiResponse<?> updateReviewHomestay(Long userId, Long reviewId, ReviewDTO reviewDTO) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Review id is required");
        }
        if (reviewDTO == null) {
            throw new IllegalArgumentException("Review data is required");
        }
        if (reviewDTO.getRating() != null && (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (reviewDTO.getComment() != null && reviewDTO.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        // Lấy user và customer
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));

        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        // Tìm review theo ID
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for id " + reviewId));

        // Validate: Review phải thuộc về customer hiện tại
        if (!review.getCustomer().getId().equals(customer.getId())) {
            return new ApiResponse<>(403, "You can only update your own reviews", null);
        }

        // Lấy homestay từ review
        Homestay homestay = review.getHomestay();
        if (homestay == null) {
            return new ApiResponse<>(404, "Homestay not found for this review", null);
        }

        // Update rating nếu có
        if (reviewDTO.getRating() != null) {
            review.setRating(reviewDTO.getRating());
        }

        // Update comment nếu có
        if (reviewDTO.getComment() != null) {
            review.setComment(reviewDTO.getComment().trim());
        }

        // Xử lý images: xóa images cũ và thêm images mới
        if (reviewDTO.getImageUrls() != null) {
            // Xóa tất cả images cũ của review
            Set<Image> oldImages = review.getListImage();
            if (oldImages != null && !oldImages.isEmpty()) {
                // Set review = null cho các images cũ trước khi xóa
                oldImages.forEach(image -> image.setReview(null));
                imageRepository.deleteAll(oldImages);
            }

            // Thêm images mới nếu có
            Set<Image> newImages = new HashSet<>();
            if (!reviewDTO.getImageUrls().isEmpty()) {
                final Review reviewForImages = review; // Make final for lambda
                List<Image> imagesToSave = reviewDTO.getImageUrls().stream()
                        .filter(img -> img != null && img.getImage_url() != null && !img.getImage_url().trim().isEmpty())
                        .map(img -> {
                            Image image = ImageMapper.toEntity(img);
                            image.setReview(reviewForImages);
                            return imageRepository.save(image);
                        })
                        .collect(Collectors.toList());
                newImages.addAll(imagesToSave);
            }
            review.setListImage(newImages);
        }

        // Lưu review đã update
        Review updatedReview = reviewRepository.save(review);

        // Tính lại rating trung bình của homestay
        // List<Review> allReviews = reviewRepository.findByHomestay(homestay);
        // if (!allReviews.isEmpty()) {
        //     double averageRating = allReviews.stream()
        //             .mapToInt(Review::getRating)
        //             .average()
        //             .orElse(0.0);
        //     homestay.setRating((float) averageRating);
        //     homestayRepository.save(homestay);
        // }

        // Map sang DTO để trả về
        ReviewDTO responseDTO = ReviewMapper.toDTO(updatedReview);

        return new ApiResponse<>(200, "Review updated successfully", responseDTO);
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCustomerBills(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        // Lấy user và customer
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));

        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        // Lấy tất cả bills của customer, chỉ lấy những bills đã được thanh toán hoặc hoàn tất
        List<Bill> bills = billRepository.findByCustomer(customer);
        
        // Filter chỉ lấy các bills đã hoàn tất hoặc đã check-in (có thể review được)
        List<Bill> completedBills = bills.stream()
//                .filter(bill -> {
//                    StatusBill status = bill.getStatus();
//                    return status == StatusBill.SUCCEED
//                            || status == StatusBill.CHECKIN_EXPIRED
//                            || status == StatusBill.COMPLAINT_PENDING
//                            || status == StatusBill.CHECKIN_PENDING;
//                })
                .collect(Collectors.toList());

        // Map sang BillDTO
        List<BillDTO> billDTOS = completedBills.stream()
                .map(BillMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Customer bills retrieved successfully", billDTOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCustomerOrders(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));

        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        List<Bill> bills = billRepository.findByCustomer(customer);
        Comparator<Bill> byCreatedAtDesc = Comparator
                .comparing(Bill::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();

        List<CustomerOrderResponse> responses = bills.stream()
                .sorted(byCreatedAtDesc)
                .map(this::mapToCustomerOrder)
                .toList();

        return new ApiResponse<>(200, "Customer orders retrieved successfully", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCustomerOrderDetail(Long userId, Long billId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (billId == null) {
            throw new IllegalArgumentException("Bill id is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));

        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        Bill bill = billRepository.findById(billId)
                .orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + billId, null);
        }

        if (bill.getCustomer() == null || !Objects.equals(bill.getCustomer().getId(), customer.getId())) {
            return new ApiResponse<>(403, "You can only view details for your own bills", null);
        }

        List<Transaction> transactions = transactionRepository.findByBill(bill);

        CustomerOrderPaymentStatusResponse paymentStatus = buildPaymentStatusSnapshot(bill, transactions);
        CustomerOrderComplaintStatusResponse complaintStatus = buildComplaintStatusSnapshot(bill);
        CustomerOrderActionPermissionResponse actions = buildActionPermissions(bill, paymentStatus, complaintStatus);

        CustomerOrderDetailResponse response = CustomerOrderDetailResponse.builder()
                .summary(mapToCustomerOrder(bill))
                .paymentStatus(paymentStatus)
                .complaintStatus(complaintStatus)
                .actions(actions)
                .build();

        return new ApiResponse<>(200, "Customer order detail retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCustomerComplaints(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));

        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        List<Complaint> complaints = complaintRepository.findByBill_Customer(customer);
        Comparator<Complaint> byCreatedAtDesc = Comparator
                .comparing(Complaint::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();

        List<CustomerComplaintResponse> responses = complaints.stream()
                .sorted(byCreatedAtDesc)
                .map(this::mapToCustomerComplaint)
                .toList();

        return new ApiResponse<>(200, "Customer complaints retrieved successfully", responses);
    }

    private CustomerOrderResponse mapToCustomerOrder(Bill bill) {
        Homestay homestay = bill.getHomestay();
        return CustomerOrderResponse.builder()
                .billId(bill.getId())
                .billCode(bill.getCode())
                .status(bill.getStatus())
                .homestayId(homestay != null ? homestay.getId() : null)
                .homestayName(homestay != null ? homestay.getTitle() : null)
                .checkIn(bill.getCheckIn())
                .checkOut(bill.getCheckOut())
                .totalAmount(bill.getTotalAmount())
                .depositAmount(resolveDepositAmount(bill))
                .createdAt(bill.getCreatedAt())
                .basePrice(homestay != null ? homestay.getBasePrice() : null)
                .dailyPrices(mapDailyPrices(bill))
                .build();
    }

    private CustomerComplaintResponse mapToCustomerComplaint(Complaint complaint) {
        Bill bill = complaint.getBill();
        Homestay homestay = bill != null ? bill.getHomestay() : null;

        return CustomerComplaintResponse.builder()
                .billId(bill != null ? bill.getId() : null)
                .billCode(bill != null ? bill.getCode() : null)
                .billStatus(bill != null ? bill.getStatus() : null)
                .homestayId(homestay != null ? homestay.getId() : null)
                .homestayName(homestay != null ? homestay.getTitle() : null)
                .checkIn(bill != null ? bill.getCheckIn() : null)
                .checkOut(bill != null ? bill.getCheckOut() : null)
                .billCreatedAt(bill != null ? bill.getCreatedAt() : null)
                .complaint(ComplaintMapper.toDTO(complaint))
                .build();
    }

    private List<CustomerOrderDailyPriceResponse> mapDailyPrices(Bill bill) {
        if (bill == null || bill.getListHomestayDailyPrices() == null) {
            return List.of();
        }

        Comparator<CustomerOrderDailyPriceResponse> byDate = Comparator
                .comparing(CustomerOrderDailyPriceResponse::getDate, Comparator.nullsLast(Comparator.naturalOrder()));

        return bill.getListHomestayDailyPrices().stream()
                .map(this::mapDailyPrice)
                .filter(Objects::nonNull)
                .sorted(byDate)
                .toList();
    }

    private CustomerOrderDailyPriceResponse mapDailyPrice(HomestayDailyPrice entity) {
        if (entity == null) {
            return null;
        }

        LocalDate date = null;
        if (entity.getPricePerDay() != null && entity.getPricePerDay().getDay() != null) {
            date = entity.getPricePerDay().getDay().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        return CustomerOrderDailyPriceResponse.builder()
                .dailyPriceId(entity.getId())
                .date(date)
                .price(entity.getPrice())
                .build();
    }

    private java.math.BigDecimal resolveDepositAmount(Bill bill) {
        if (bill == null) {
            return null;
        }

        List<Transaction> transactions = transactionRepository.findByBill(bill);
        java.math.BigDecimal depositFromTransaction = transactions.stream()
                .filter(tx -> tx.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST)
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (depositFromTransaction != null) {
            return depositFromTransaction;
        }

        return bill.getTotalAmount() != null
                ? bill.getTotalAmount().multiply(java.math.BigDecimal.valueOf(0.3))
                : null;
    }

    private CustomerOrderPaymentStatusResponse buildPaymentStatusSnapshot(Bill bill, List<Transaction> transactions) {
        List<Transaction> safeTransactions = transactions != null ? transactions : List.of();
        BigDecimal depositAmount = resolveDepositAmount(bill);
        BigDecimal totalAmount = bill.getTotalAmount();
        BigDecimal remainingAmount = null;
        if (totalAmount != null) {
            if (depositAmount != null) {
                remainingAmount = totalAmount.subtract(depositAmount).max(BigDecimal.ZERO);
            } else {
                remainingAmount = totalAmount;
            }
        }

        Transaction depositTransaction = findLatestTransaction(safeTransactions, TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST, StatusTransaction.SUCCESS);
        Transaction remainingTransaction = findLatestTransaction(safeTransactions, TypeTransaction.CUSTOMER_PAYMENT_ADMIN_SECOND, StatusTransaction.SUCCESS);
        Transaction refundSuccessTransaction = findLatestTransaction(safeTransactions, TypeTransaction.REFUND, StatusTransaction.SUCCESS);
        Transaction refundPendingTransaction = findLatestTransaction(safeTransactions, TypeTransaction.REFUND, StatusTransaction.PENDING);

        boolean depositPaid = depositTransaction != null;
        boolean remainingPaid = remainingTransaction != null;
        boolean awaitingDeposit = bill.getStatus() == StatusBill.DEPOSIT_PENDING;
        boolean remainingRequired = bill.getStatus() == StatusBill.DEPOSIT_PAID
                || bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING;
        boolean awaitingRemaining = remainingRequired && !remainingPaid;
        boolean awaitingRefund = bill.getStatus() == StatusBill.PENDING_REFUNDED || refundPendingTransaction != null;
        boolean refunded = bill.getStatus() == StatusBill.REFUNDED
                || bill.getStatus() == StatusBill.CANCELLED_REFUNDED
                || refundSuccessTransaction != null;
        boolean paymentFailed = bill.getStatus() == StatusBill.REMAINING_PAYMENT_FAILED;

        String phase = determinePaymentPhase(bill.getStatus());

        return CustomerOrderPaymentStatusResponse.builder()
                .phase(phase)
                .awaitingDeposit(awaitingDeposit)
                .depositPaid(depositPaid)
                .depositPaidAt(depositTransaction != null ? depositTransaction.getCompletedAt() : null)
                .depositAmount(depositAmount)
                .awaitingRemainingPayment(awaitingRemaining)
                .remainingPaymentRequired(remainingRequired)
                .remainingPaid(remainingPaid)
                .remainingPaidAt(remainingTransaction != null ? remainingTransaction.getCompletedAt() : null)
                .remainingAmount(remainingAmount)
                .paymentFailed(paymentFailed)
                .awaitingRefund(awaitingRefund)
                .refunded(refunded)
                .refundCompletedAt(refundSuccessTransaction != null ? refundSuccessTransaction.getCompletedAt() : null)
                .build();
    }

    private static final List<StatusBill> STATUS_TIMELINE = List.of(
            StatusBill.DEPOSIT_PENDING,
            StatusBill.DEPOSIT_PAID,
            StatusBill.REMAINING_PAYMENT_PENDING,
            StatusBill.REMAINING_PAYMENT_FAILED,
            StatusBill.CHECKIN_EXPIRED,
            StatusBill.COMPLAINT_PENDING,
            StatusBill.HOST_COMPLAINT_PROCESSING,
            StatusBill.ADMIN_COMPLAINT_PROCESSING,
            StatusBill.PENDING_REFUNDED,
            StatusBill.REFUNDED,
            StatusBill.REJECTED,
            StatusBill.SUCCEED,
            StatusBill.CANCELLED_REFUNDED,
            StatusBill.CANCELLED
    );

    private CustomerOrderComplaintStatusResponse buildComplaintStatusSnapshot(Bill bill) {
        StatusBill status = bill.getStatus();
        LocalDateTime complaintDeadline = calculateComplaintDeadline(bill);
        LocalDateTime now = LocalDateTime.now();
        boolean withinDeadline = complaintDeadline != null
                && (now.isBefore(complaintDeadline) || now.isEqual(complaintDeadline));

        boolean inComplaintWindow = status == StatusBill.COMPLAINT_PENDING;
        boolean underHostReview = status == StatusBill.HOST_COMPLAINT_PROCESSING;
        boolean underAdminReview = status == StatusBill.ADMIN_COMPLAINT_PROCESSING;
        boolean refundInProgress = status == StatusBill.PENDING_REFUNDED;
        boolean resolvedWithRefund = status == StatusBill.REFUNDED;
        boolean resolvedWithoutRefund = status == StatusBill.REJECTED;

        boolean complaintRelated = status == StatusBill.COMPLAINT_PENDING
                || status == StatusBill.HOST_COMPLAINT_PROCESSING
                || status == StatusBill.ADMIN_COMPLAINT_PROCESSING
                || status == StatusBill.PENDING_REFUNDED
                || status == StatusBill.REFUNDED
                || status == StatusBill.REJECTED;

        Complaint latestComplaint = findLatestComplaint(bill);
        boolean hasActiveComplaint = latestComplaint != null;
        boolean canFileComplaint = withinDeadline && status == StatusBill.COMPLAINT_PENDING;
        boolean canCancelComplaint = status == StatusBill.HOST_COMPLAINT_PROCESSING && hasActiveComplaint;
        boolean canUpdateComplaint = canCancelComplaint;

        return CustomerOrderComplaintStatusResponse.builder()
                .phase(determineComplaintPhase(status))
                .complaintRelated(complaintRelated)
                .inComplaintWindow(inComplaintWindow)
                .underHostReview(underHostReview)
                .underAdminReview(underAdminReview)
                .refundInProgress(refundInProgress)
                .resolvedWithRefund(resolvedWithRefund)
                .resolvedWithoutRefund(resolvedWithoutRefund)
                .complaintDeadline(complaintDeadline)
                .withinComplaintDeadline(withinDeadline)
                .latestComplaintId(hasActiveComplaint ? latestComplaint.getId() : null)
                .canFileComplaint(canFileComplaint)
                .canCancelComplaint(canCancelComplaint)
                .canUpdateComplaint(canUpdateComplaint)
                .build();
    }

    private CustomerOrderActionPermissionResponse buildActionPermissions(
            Bill bill,
            CustomerOrderPaymentStatusResponse paymentStatus,
            CustomerOrderComplaintStatusResponse complaintStatus
    ) {
        StatusBill status = bill.getStatus();
        boolean canCancel = isStatusBeforeOrEqual(status, StatusBill.COMPLAINT_PENDING);
        boolean canPayRemaining = status == StatusBill.REMAINING_PAYMENT_PENDING;
        boolean canCheckIn = status == StatusBill.REMAINING_PAYMENT_PENDING && paymentStatus.isDepositPaid();

        return CustomerOrderActionPermissionResponse.builder()
                .canCancel(canCancel)
                .canPayRemaining(canPayRemaining)
                .canCheckIn(canCheckIn)
                .canFileComplaint(complaintStatus.isCanFileComplaint())
                .build();
    }

    private String determinePaymentPhase(StatusBill status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return switch (status) {
            case DEPOSIT_PENDING -> "WAITING_DEPOSIT";
            case DEPOSIT_PAID -> "DEPOSIT_PAID_WAITING_CHECKIN";
            case REMAINING_PAYMENT_PENDING -> "AWAITING_REMAINING_PAYMENT";
            case REMAINING_PAYMENT_FAILED -> "REMAINING_PAYMENT_FAILED";
            case CHECKIN_EXPIRED -> "CHECKIN_EXPIRED";
            case COMPLAINT_PENDING -> "COMPLAINT_WINDOW";
            case HOST_COMPLAINT_PROCESSING -> "HOST_REVIEW";
            case ADMIN_COMPLAINT_PROCESSING -> "ADMIN_REVIEW";
            case PENDING_REFUNDED -> "REFUND_PENDING";
            case REFUNDED -> "REFUNDED";
            case REJECTED -> "COMPLAINT_REJECTED";
            case SUCCEED -> "COMPLETED";
            case CANCELLED_REFUNDED -> "CANCELLED_REFUNDED";
            case CANCELLED -> "CANCELLED";
        };
    }

    private String determineComplaintPhase(StatusBill status) {
        if (status == null) {
            return "NONE";
        }
        return switch (status) {
            case COMPLAINT_PENDING -> "WINDOW";
            case HOST_COMPLAINT_PROCESSING -> "HOST_REVIEW";
            case ADMIN_COMPLAINT_PROCESSING -> "ADMIN_REVIEW";
            case PENDING_REFUNDED -> "REFUND_PENDING";
            case REFUNDED, CANCELLED_REFUNDED -> "RESOLVED_REFUNDED";
            case REJECTED, SUCCEED, CANCELLED -> "RESOLVED";
            default -> "NONE";
        };
    }

    private Complaint findLatestComplaint(Bill bill) {
        if (bill == null) {
            return null;
        }
        return complaintRepository.findTopByBillOrderByCreatedAtDesc(bill)
                .orElse(null);
    }

    private LocalDateTime calculateComplaintDeadline(Bill bill) {
        if (bill == null || bill.getCheckIn() == null || bill.getCheckOut() == null) {
            return null;
        }

        long nights = DAYS.between(
                bill.getCheckIn().toLocalDate(),
                bill.getCheckOut().toLocalDate()
        );
        if (nights < 0) {
            nights = 0;
        }

        return bill.getCheckOut().plusDays( 1);
    }

    private Transaction findLatestTransaction(List<Transaction> transactions, TypeTransaction type, StatusTransaction status) {
        if (transactions == null || transactions.isEmpty()) {
            return null;
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(tx -> tx.getTransactionType() == type)
                .filter(tx -> status == null || tx.getStatus() == status)
                .max(Comparator.comparing(
                        tx -> {
                            LocalDateTime completedAt = tx.getCompletedAt();
                            return completedAt != null ? completedAt : tx.getUpdatedAt();
                        },
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .orElse(null);
    }

    private boolean isStatusBeforeOrEqual(StatusBill status, StatusBill target) {
        if (status == null || target == null) {
            return false;
        }
        int currentIndex = STATUS_TIMELINE.indexOf(status);
        int targetIndex = STATUS_TIMELINE.indexOf(target);
        if (currentIndex == -1 || targetIndex == -1) {
            return false;
        }
        return currentIndex <= targetIndex;
    }


    // Chưa checkin đúng hạn nên bị hủy
    @Override
    @Transactional
    public ApiResponse<?> cancelBill(Long userId, Long billId) {
        // Tìm bill
        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + billId, null);
        }

        // Validate: Bill phải thuộc về customer này
        Customer customer = customerRepository.findByUser(userRepository.findById(userId).orElse(null));
        if (customer == null || !Objects.equals(bill.getCustomer().getId(), customer.getId())) {
            return new ApiResponse<>(403, "You can only cancel your own bills", null);
        }

        // Validate: Bill phải ở trạng thái DEPOSIT_PENDING hoặc DEPOSIT_PAID hoặc REMAINING_PAYMENT_PENDING
        /* TODO
        Kiểm tra xem những trạng thái nào thì được cancel bill
         */
        if (bill.getStatus() != StatusBill.DEPOSIT_PENDING 
                && bill.getStatus() != StatusBill.DEPOSIT_PAID 
                && bill.getStatus() != StatusBill.REMAINING_PAYMENT_PENDING) {
            return new ApiResponse<>(400, "Bill cannot be cancelled. Current status: " + bill.getStatus(), null);
        }

        // Kiểm tra thời gian hủy: nếu hủy trước 2 ngày so với check-in thì được hoàn tiền
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkIn = bill.getCheckIn();
        long daysUntilCheckIn = DAYS.between(now, checkIn);
        boolean canRefund = daysUntilCheckIn >= 2;

        // Unlock homestay_daily_prices
        List<HomestayDailyPrice> dailyPrices = homestayDailyPricesRepository.findAll().stream()
                .filter(hdp -> hdp.getBill() != null && hdp.getBill().getId().equals(bill.getId()))
                .toList();

        for (HomestayDailyPrice dailyPrice : dailyPrices) {
            dailyPrice.setIsBooked(false);
            dailyPrice.setBill(null);
            homestayDailyPricesRepository.save(dailyPrice);
        }

        // Nếu được hoàn tiền, tạo transaction REFUND
        if (canRefund) {
            // Tìm transaction thanh toán cọc đã thành công
            Transaction depositTransaction = transactionRepository.findByBillId(bill.getId()).stream()
                    .filter(t -> t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST
                            && t.getStatus() == StatusTransaction.SUCCESS)
                    .findFirst()
                    .orElse(null);

            if (depositTransaction != null) {
                // Lấy admin user
                User adminUser = userRepository.findAll().stream()
                        .filter(u -> u.getAdmin() != null)
                        .findFirst()
                        .orElse(null);

                if (adminUser != null) {
                    // Tạo transaction REFUND với status PENDING (chờ admin xử lý)
                    Transaction refundTransaction = Transaction.builder()
                            .amount(depositTransaction.getAmount()) // Hoàn lại số tiền đã thanh toán
                            .transactionType(TypeTransaction.REFUND)
                            .status(StatusTransaction.PENDING) // Chờ admin xử lý
                            .bill(bill)
                            .fromUser(adminUser)
                            .toUser(customer.getUser())
                            .completedAt(null) // Chưa hoàn tất, chờ admin xác nhận
                            .build();
                    transactionRepository.save(refundTransaction);
                }
            }

            bill.setStatus(StatusBill.CANCELLED_REFUNDED);
        }
        else
            // Cập nhật status bill thành CANCELLED do khong duoc hoang tien
            bill.setStatus(StatusBill.CANCELLED);
        billRepository.save(bill);

        String message = canRefund 
                ? "Bill cancelled successfully. Refund will be processed." 
                : "Bill cancelled successfully. No refund as cancellation is less than 2 days before check-in.";

        return new ApiResponse<>(200, message, null);
    }

    @Override
    @Transactional
    public ApiResponse<?> createComplaint(Long userId, ComplaintDTO complaintDTO) {
        // Validate input
        if (userId == null) {
            return new ApiResponse<>(400, "User ID is required", null);
        }
        if (complaintDTO == null) {
            return new ApiResponse<>(400, "Complaint data is required", null);
        }
        if (complaintDTO.getBillId() == null) {
            return new ApiResponse<>(400, "Bill ID is required", null);
        }
        if (complaintDTO.getDescription() == null || complaintDTO.getDescription().trim().isEmpty()) {
            return new ApiResponse<>(400, "Description is required", null);
        }

        // Tìm bill
        Bill bill = billRepository.findById(complaintDTO.getBillId()).orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + complaintDTO.getBillId(), null);
        }

        // Validate: Bill phải thuộc về customer này
        Customer customer = customerRepository.findByUser(userRepository.findById(userId).orElse(null));
        if (customer == null || !Objects.equals(bill.getCustomer().getId(), customer.getId())) {
            return new ApiResponse<>(403, "You can only create complaints for your own bills", null);
        }

        // Validate: Bill phải ở trạng thái COMPLAINT_PENDING (sau checkout)
        if (bill.getStatus() != StatusBill.COMPLAINT_PENDING && bill.getStatus() != StatusBill.SUCCEED) {
            return new ApiResponse<>(400, "Bill must be in COMPLAINT_PENDING status to create complaint. Current status: " + bill.getStatus(), null);
        }

        // Tính N = số ngày đặt phòng (từ checkIn đến checkOut)
        LocalDateTime checkIn = bill.getCheckIn();
        LocalDateTime checkOut = bill.getCheckOut();
        long numberOfDays = DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        
        // Thời gian cho phép khiếu nại: (N + 1) ngày sau checkout
        LocalDateTime checkoutTime = bill.getCheckOut();
        LocalDateTime complaintDeadline = checkoutTime.plusDays(numberOfDays + 1);
        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra xem có trong thời gian cho phép khiếu nại không
        if (now.isAfter(complaintDeadline)) {
            return new ApiResponse<>(422, 
                    String.format("Complaint deadline has passed. You can only file a complaint within %d days after checkout. Deadline: %s", 
                            numberOfDays + 1, complaintDeadline), 
                    null);
        }

        // Kiểm tra xem đã có complaint cho bill này chưa
//        List<Complaint> existingComplaints = complaintRepository.findByBill(bill);
//        if (!existingComplaints.isEmpty()) {
//            return new ApiResponse<>(400, "A complaint already exists for this bill", null);
//        }

        // Lấy admin đầu tiên để assign vào complaint (theo yêu cầu entity)
        Admin admin = adminRepository.findAll().stream()
                .findFirst()
                .orElse(null);
        if (admin == null) {
            return new ApiResponse<>(500, "No admin found to process complaint", null);
        }

        // Lưu images
        Set<Image> images = new HashSet<>();
        if (complaintDTO.getImageUrls() != null && !complaintDTO.getImageUrls().isEmpty()) {
            complaintDTO.getImageUrls().forEach(imageUrl -> {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    Image image = Image.builder()
                            .image_url(imageUrl)
                            .build();
                    images.add(imageRepository.save(image));
                }
            });
        }

        // Tạo complaint
        Complaint complaint = Complaint.builder()
                .bill(bill)
                .admin(admin)
                .description(complaintDTO.getDescription())
                .listImage(images)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Cập nhật status bill (nếu cần - có thể đã là HOST_COMPLAINT_PROCESSING rồi)
        bill.setStatus(StatusBill.HOST_COMPLAINT_PROCESSING);
        billRepository.save(bill);

        log.info("Complaint created successfully for bill {} by customer {}. Days allowed: {}, Deadline: {}", 
                bill.getId(), userId, numberOfDays + 1, complaintDeadline);

        // Map entity sang DTO để tránh circular reference
        ComplaintDTO resultDTO = ComplaintMapper.toDTO(savedComplaint);

        return new ApiResponse<>(200, "Complaint created successfully", resultDTO);
    }

    @Override
    @Transactional
    public ApiResponse<?> updateComplaint(Long userId, Long complaintId, ComplaintDTO complaintDTO) {
        // Validate input
        if (userId == null) {
            return new ApiResponse<>(400, "User ID is required", null);
        }
        if (complaintId == null) {
            return new ApiResponse<>(400, "Complaint ID is required", null);
        }
        if (complaintDTO == null) {
            return new ApiResponse<>(400, "Complaint data is required", null);
        }
        if (complaintDTO.getDescription() == null || complaintDTO.getDescription().trim().isEmpty()) {
            return new ApiResponse<>(400, "Description is required", null);
        }

        // Tìm complaint
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null) {
            return new ApiResponse<>(404, "Complaint not found with id: " + complaintId, null);
        }

        // Validate: Complaint phải thuộc về customer này
        Bill bill = complaint.getBill();
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found for this complaint", null);
        }

        Customer customer = customerRepository.findByUser(userRepository.findById(userId).orElse(null));
        if (customer == null || !Objects.equals(bill.getCustomer().getId(), customer.getId())) {
            return new ApiResponse<>(403, "You can only update your own complaints", null);
        }

        // Validate: Chỉ có thể update khi bill ở trạng thái HOST_COMPLAINT_PROCESSING
        if (bill.getStatus() != StatusBill.HOST_COMPLAINT_PROCESSING) {
            return new ApiResponse<>(400,
                    "Complaint can only be updated when bill status is HOST_COMPLAINT_PROCESSING",
                    null);
        }

        Complaint latestComplaint = findLatestComplaint(bill);
        if (latestComplaint == null || !Objects.equals(latestComplaint.getId(), complaint.getId())) {
            return new ApiResponse<>(400, "Only the latest complaint for this bill can be updated", null);
        }

        // Cập nhật description
        complaint.setDescription(complaintDTO.getDescription());

        // Cập nhật images nếu có
        if (complaintDTO.getImageUrls() != null) {
            // Xóa các images cũ liên quan đến complaint này
            Set<Image> oldImages = complaint.getListImage();
            if (oldImages != null && !oldImages.isEmpty()) {
                for (Image oldImage : oldImages) {
                    oldImage.setComplaint(null);
                    imageRepository.save(oldImage);
                }
            }

            // Tạo images mới
            Set<Image> newImages = new HashSet<>();
            complaintDTO.getImageUrls().forEach(imageUrl -> {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    Image image = Image.builder()
                            .image_url(imageUrl)
                            .complaint(complaint)
                            .build();
                    newImages.add(imageRepository.save(image));
                }
            });
            complaint.setListImage(newImages);
        }

        Complaint updatedComplaint = complaintRepository.save(complaint);

        log.info("Complaint {} updated successfully by customer {}", complaintId, userId);

        // Map entity sang DTO để tránh circular reference
        ComplaintDTO resultDTO = ComplaintMapper.toDTO(updatedComplaint);

        return new ApiResponse<>(200, "Complaint updated successfully", resultDTO);
    }

    @Override
    @Transactional
    public ApiResponse<?> cancelComplaint(Long userId, Long billId, CancelComplaintRequest request) {
        if (userId == null) {
            return new ApiResponse<>(400, "User ID is required", null);
        }
        if (billId == null) {
            return new ApiResponse<>(400, "Bill ID is required", null);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "User not found with id: " + userId, null);
        }

        Customer customer = customerRepository.findByUser(user);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found for this user", null);
        }

        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + billId, null);
        }

        if (bill.getCustomer() == null || !Objects.equals(bill.getCustomer().getId(), customer.getId())) {
            return new ApiResponse<>(403, "You can only cancel complaints for your own bills", null);
        }

        if (bill.getStatus() != StatusBill.HOST_COMPLAINT_PROCESSING) {
            return new ApiResponse<>(400,
                    "Complaint can only be cancelled when bill status is HOST_COMPLAINT_PROCESSING",
                    null);
        }

        Complaint latestComplaint = findLatestComplaint(bill);
        if (latestComplaint == null) {
            return new ApiResponse<>(400, "No active complaint found for this bill", null);
        }

        String reason = request != null ? request.getReason() : null;
        if (reason != null && reason.trim().isEmpty()) {
            reason = null;
        }

        Set<Image> images = latestComplaint.getListImage();
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                image.setComplaint(null);
                imageRepository.delete(image);
            }
        }

        complaintRepository.delete(latestComplaint);

        bill.setStatus(StatusBill.COMPLAINT_PENDING);
        billRepository.save(bill);

        log.info("Complaint {} cancelled by customer {} for bill {}. Reason: {}", latestComplaint.getId(), userId, billId, reason);

        Map<String, Object> data = Map.of(
                "billId", bill.getId(),
                "newBillStatus", bill.getStatus(),
                "complaintId", latestComplaint.getId(),
                "reason", reason
        );

        return new ApiResponse<>(200, "Complaint cancelled successfully", data);
    }
}
