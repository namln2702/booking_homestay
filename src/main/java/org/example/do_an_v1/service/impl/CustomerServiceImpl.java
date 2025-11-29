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
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.example.do_an_v1.utils.Date;
import org.example.do_an_v1.utils.GenNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public ApiResponse<?> booking(BillDTO billDTO) {

        Homestay homestay = homestayRepository.findById(billDTO.getId()).orElseThrow(() -> new RuntimeException("Homestay not exits"));
        String start = Date.DateToString(billDTO.getCheckIn());
        String end = Date.DateToString(billDTO.getCheckOut());




        // Check homestay availability
        if(homestayDailyPricesRepository.checkHomestayAvailability(homestay.getId(), start, end))
            return new ApiResponse<>(422, "Room has been booked", null );


        // Save bill
        Bill bill = BillMapper.toEntity(billDTO);
        Bill billResult = billRepository.save(bill);

        // Save customer into bill
        Customer customer = CustomerMapper.toEntity(billDTO.getCustomerDTO());
        customer.setId(Long.parseLong( (String) sessionConfig.httpSession().getAttribute("id")));
        billResult.setCustomer(customer);


        // Save HomestayDailyPrices into bill
        Bill finalBillResult = billResult;
        billDTO.getHomestayDailyPricesDTOS()
                .forEach(pricePerDayDTO -> {
                    homestayDailyPricesRepository.save(HomestayDailyPrice.builder()
                                    .isBooked(true)
                                    .price(pricePerDayDTO.getPrice())
                                    .homestay(homestay)
                                    .bill(finalBillResult)
                            .build());
                });


        // luu thong tin CustomerBookingInfo neu la nguoi moi
        if(!Objects.isNull(billDTO.getCustomerBookingInfoDTO())){
            CustomerBookingInfo customerBookingInfo = CustomerBookingInfoMapper.toEntity(billDTO.getCustomerBookingInfoDTO());
            customerBookingInfo = customerBookingInfoRepository.save(customerBookingInfo);
            billResult.setCustomerBookingInfo(customerBookingInfo);
        }


        // Create transaction
        Transaction transaction = Transaction.builder()
                .completedAt(LocalDateTime.now().plusMinutes(15))
                .transactionType(TypeTransaction.BOOKING_PAYMENT)
                .status(StatusTransaction.PENDING)
                .bill(billResult)
                .fromUser(customer.getUser())
                .build();


        // Create code
        bill.setCode(GenNumber.generate());
        bill.setStatus(StatusBill.PAYMENT_PENDING);
        billResult = billRepository.save(billResult);

        return new ApiResponse<>(200, "Save bill success", billResult);
    }

    public ApiResponse<?> payment(PaymentDTO paymentDTO){
        return null;
    }

    @Override
    public ApiResponse<?> updateCustomer(CustomerDTO customerDTO) {

        Long userId = customerDTO.getIdCustomer();
        Customer customer = customerRepository.findById(userId).orElseThrow( () -> new RuntimeException("Customer not exits"));
        Set<Preference> preferenceList = customer.getListPreferences();


        customerDTO.getListPreference().forEach(idPreference ->
        {
            Preference preference = preferenceRepository.findById(idPreference).orElseThrow(() -> new RuntimeException("Preference not exits"));
            preferenceList.add(preference);
        });
        customer.setListPreferences(preferenceList);
        customer.setStatus(Status.ACTIVE);
        return new ApiResponse<>(200, "Success", customerRepository.save(customer));

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

}
