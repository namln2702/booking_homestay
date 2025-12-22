package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.configuration.SessionConfig;
import org.example.do_an_v1.dto.*;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.*;
import org.example.do_an_v1.mapper.*;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.CustomerService;
import org.example.do_an_v1.service.EmailService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
//import org.example.do_an_v1.utils.Date;
import org.example.do_an_v1.utils.GenNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Request có thể là List<PreferenceDTO> hoặc List<Long> (tương thích ngược)
        Set<Preference> newPreferences = new HashSet<>();
        if (customerDTO.getListPreference() != null) {
            for (org.example.do_an_v1.dto.PreferenceDTO preferenceDTO : customerDTO.getListPreference()) {
                if (preferenceDTO == null || preferenceDTO.getId() == null) {
                    continue; // Bỏ qua null values
                }
                Preference preference = preferenceRepository.findById(preferenceDTO.getId()).orElse(null);
                if (preference == null) {
                    return new ApiResponse<>(404, "Preference not found with id: " + preferenceDTO.getId(), null);
                }
                newPreferences.add(preference);
            }
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

        // Validate: Chỉ có thể update khi bill ở trạng thái COMPLAINT_PENDING hoặc HOST_COMPLAINT_PROCESSING
        // (chưa được xử lý bởi admin)
        if ( bill.getStatus() != StatusBill.HOST_COMPLAINT_PROCESSING) {
            return new ApiResponse<>(400, 
                    "Cannot update complaint. Bill is already being processed by admin or has been resolved. Current status: " + bill.getStatus(), 
                    null);
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
}
