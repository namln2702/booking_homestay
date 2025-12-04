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
        bill.setCode(GenNumber.generate());
        bill.setStatus(StatusBill.PAYMENT_PENDING);

        Bill billResult = billRepository.save(bill);


        // Lấy danh sách HomestayDailyPrice để khóa
        List<HomestayDailyPrice> finalDailyPricesToLock = new ArrayList<>();
        
        // Nếu có danh sách ID được truyền vào, sử dụng chúng
        if (bookingDTO.getHomestayDailyPriceIds() != null && !bookingDTO.getHomestayDailyPriceIds().isEmpty()) {
            // Lấy các HomestayDailyPrice theo danh sách ID
            for (Long dailyPriceId : bookingDTO.getHomestayDailyPriceIds()) {
                if (dailyPriceId == null) {
                    continue;
                }
                
                HomestayDailyPrice dailyPrice = homestayDailyPricesRepository.findById(dailyPriceId).orElse(null);
                if (dailyPrice == null) {
                    return new ApiResponse<>(404, "HomestayDailyPrice not found with id: " + dailyPriceId, null);
                }
                
                // Validate: HomestayDailyPrice phải thuộc về homestay đang booking
                if (!dailyPrice.getHomestay().getId().equals(homestay.getId())) {
                    return new ApiResponse<>(400, "HomestayDailyPrice with id " + dailyPriceId + " does not belong to homestay " + homestay.getId(), null);
                }
                
                finalDailyPricesToLock.add(dailyPrice);
            }
        } else {
            // Nếu không có danh sách ID, tự động tìm theo khoảng thời gian check-in đến check-out
            // Sử dụng lại startDate và endDate đã tạo ở trên
            // Tìm các HomestayDailyPrice trong khoảng thời gian này
            List<HomestayDailyPrice> dailyPricesToLock = homestayDailyPricesRepository
                    .findByHomestayAndDateRange(homestay.getId(), startDate, endDate);
            
            // Tạo map để dễ dàng kiểm tra daily price đã tồn tại cho từng ngày
            Map<LocalDate, HomestayDailyPrice> existingDailyPricesMap = new HashMap<>();
            for (HomestayDailyPrice dailyPrice : dailyPricesToLock) {
                LocalDate priceDate = dailyPrice.getPricePerDay().getDay().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                existingDailyPricesMap.put(priceDate, dailyPrice);
            }
            
            // Lấy giá mặc định từ homestay (basePrice)
            Float defaultPrice = homestay.getBasePrice() != null ? homestay.getBasePrice() : 0f;
            
            // Duyệt qua từng ngày từ check-in đến check-out (không bao gồm check-out)
            LocalDate currentDate = checkInLocalDate;
            while (currentDate.isBefore(checkOutLocalDate)) {
                HomestayDailyPrice dailyPrice = existingDailyPricesMap.get(currentDate);
                
                if (dailyPrice == null) {
                    // Chưa có daily price cho ngày này, tạo mới
                    java.util.Date currentDateUtil = java.sql.Date.valueOf(currentDate);
                    
                    // Tìm hoặc tạo PricePerDay cho ngày này
                    PricePerDay pricePerDay = pricePerDayRepository.findByDay(currentDateUtil)
                            .orElseGet(() -> {
                                PricePerDay newPricePerDay = PricePerDay.builder()
                                        .day(currentDateUtil)
                                        .price(defaultPrice)
                                        .build();
                                return pricePerDayRepository.save(newPricePerDay);
                            });
                    
                    // Tạo HomestayDailyPrice mới
                    dailyPrice = HomestayDailyPrice.builder()
                            .price(pricePerDay.getPrice() != null ? pricePerDay.getPrice() : defaultPrice)
                            .isBooked(Boolean.FALSE)
                            .pricePerDay(pricePerDay)
                            .homestay(homestay)
                            .build();
                    dailyPrice = homestayDailyPricesRepository.save(dailyPrice);
                }
                
                finalDailyPricesToLock.add(dailyPrice);
                currentDate = currentDate.plusDays(1);
            }
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

        // Create transaction
        Transaction transaction = Transaction.builder()
                .completedAt(LocalDateTime.now().plusMinutes(15))
                .transactionType(TypeTransaction.BOOKING_PAYMENT)
                .status(StatusTransaction.PENDING)
                .bill(billResult)
                .fromUser(customer.getUser())
                .toUser(adminUser)
                .amount(java.math.BigDecimal.valueOf(
                        finalDailyPricesToLock.stream()
                                .mapToDouble(HomestayDailyPrice::getPrice)
                                .sum()
                ))
                .build();
        
        transactionRepository.save(transaction);


        // Create code
//        bill.setCode(GenNumber.generate());
//        bill.setStatus(StatusBill.PAYMENT_PENDING);
        billResult = billRepository.save(billResult);

        // Reload bill với đầy đủ thông tin để map sang DTO
        Bill billWithDetails = billRepository.findById(billResult.getId()).orElse(null);
        if (billWithDetails == null) {
            return new ApiResponse<>(500, "Bill not found after save", null);
        }

        // Convert sang BillDTO để trả về
        BillDTO billDTO = BillMapper.toDTO(billWithDetails);

        // Gửi email mã code
        String emailToSend;
        if (billWithDetails.getCustomerBookingInfo() != null && 
            billWithDetails.getCustomerBookingInfo().getEmail() != null) {
            // Nếu có customerBookingInfo, gửi đến email của customerBookingInfo
            emailToSend = billWithDetails.getCustomerBookingInfo().getEmail();
        } else {
            // Nếu không có, gửi đến email của customer
            emailToSend = customer.getUser().getEmail();
        }

        // Gửi email mã code booking
        if (emailToSend != null && !emailToSend.trim().isEmpty()) {
            String emailContent = String.format(
                "Mã đặt phòng của bạn: %s\n\n" +
                "Thông tin đặt phòng:\n" +
                "- Homestay: %s\n" +
                "- Check-in: %s\n" +
                "- Check-out: %s\n" +
                "- Mã đơn: %s\n\n" +
                "Vui lòng sử dụng mã này để check-in.",
                billWithDetails.getCode(),
                homestay.getTitle(),
                billWithDetails.getCheckIn(),
                billWithDetails.getCheckOut(),
                billWithDetails.getCode()
            );
            emailService.sendSimpleEmail(emailToSend, emailContent);
        }

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
                && billStatus != StatusBill.CHECKIN_PENDING
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

    @Transactional
    public ApiResponse<?> confirmCheckin(org.example.do_an_v1.dto.request.CheckinRequest request) {
        if (request == null || request.getBillId() == null) {
            throw new IllegalArgumentException("Bill ID is required");
        }

        Bill bill = billRepository.findById(request.getBillId()).orElse(null);
        if (bill == null) {
            return new ApiResponse<>(404, "Bill not found with id: " + request.getBillId(), null);
        }

        // Validate: Mã code phải khớp với code của bill
        if (request.getCode() == null || !request.getCode().equals(bill.getCode())) {
            throw new IllegalArgumentException("Invalid check-in code");
        }

        // Validate: Bill phải ở trạng thái CHECKIN_PENDING
        if (bill.getStatus() != StatusBill.CHECKIN_PENDING) {
            throw new IllegalStateException("Bill must be in CHECKIN_PENDING status to confirm checkin. Current status: " + bill.getStatus());
        }

        // Cập nhật trạng thái bill thành COMPLAINT_PENDING
        bill.setStatus(StatusBill.COMPLAINT_PENDING);
        bill.setActualCheckinTime(LocalDateTime.now());
        billRepository.save(bill);

        return new ApiResponse<>(200, "Check-in confirmed successfully. Bill status changed to COMPLAINT_PENDING", null);
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
}
