package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.dto.request.HomestayDailyPriceRequest;
import org.example.do_an_v1.dto.request.HomestayRuleRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayPriceRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.mapper.BillMapper;
import org.example.do_an_v1.mapper.HomestayMapper;
import org.example.do_an_v1.mapper.ImageMapper;
import org.example.do_an_v1.mapper.ReviewMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.HomestayService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomestayServiceImpl implements HomestayService {

    private final HostRepository hostRepository;
    private final HomestayRepository homestayRepository;
    private final FacilitiesRepository facilitiesRepository;
    private final AmenitiesRepository amenitiesRepository;
    private final PricePerDayRepository pricePerDayRepository;
    private final HomestayImageRepository homestayImageRepository;
    private final AdminRepository adminRepository;
    private final HomestayMapper homestayMapper;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;

    @Override
    @Transactional
    public ApiResponse<HomestayDTO> createHomestay(Long hostUserId, HomestayCreateRequest request) {
        validateRequest(request);
        

        User hostUser = userRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for id " + hostUserId));

        Host host = Optional.ofNullable(hostRepository.findByUser(hostUser))
                .orElseThrow(() -> new IllegalArgumentException("Host profile not found for user " + hostUserId));

        if (homestayRepository.existsByTitleAndHost(request.getTitle(), host)) {
            throw new IllegalArgumentException("Homestay title already exists for this host");
        }

        Homestay homestay = Homestay.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .minGuest(request.getMinGuest())
                .maxGuest(request.getMaxGuest())
                .numBedrooms(request.getNumBedrooms())
                .numBeds(request.getNumBeds())
                .numBathrooms(request.getNumBathrooms())
                .numKitchen(request.getNumKitchen())
                .basePrice(request.getBasePrice() != null ? request.getBasePrice() : 0f)
                .rating(0f)
                .advancedPayment(0)
                .warningCount(0)
                .statusHomestay(StatusHomestay.PENDING)
                .host(host)
                .build();

        applyAddress(homestay, request);
        applyFacilities(homestay, request);
        applyAmenities(homestay, request);
        applyRules(homestay, request);
        applyDailyPrices(homestay, request);

        Homestay savedHomestay = homestayRepository.save(homestay);

        List<HomestayImage> savedImages = persistImages(savedHomestay, request.getImageUrls());

        HomestayDTO response = homestayMapper.toDto(savedHomestay, savedImages);
        return new ApiResponse<>(201, "Homestay created successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<HomestaySummaryDTO>>> getHomestays(
                                                                            StatusHomestay status,
                                                                            int page,
                                                                            int size) {
//        if (adminUserId == null) {
//            throw new IllegalArgumentException("Admin user id is required");
//        }

//        Admin admin = adminRepository.findById(adminUserId)
//                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for user id " + adminUserId));

//        if (admin.getStatus() != Status.ACTIVE) {
//            return new ApiResponse<>(403, "Admin account is not active", null);
//        }

        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Homestay> homestayPage = status != null
                ? homestayRepository.findByStatusHomestay(status, pageable)
                : homestayRepository.findAll(pageable);

        List<HomestaySummaryDTO> summaries = homestayPage.getContent().stream()
                .map(homestayMapper::toSummary)
                .toList();

        PageResponse<List<HomestaySummaryDTO>> pageResponse = PageResponse.<List<HomestaySummaryDTO>>builder()
                .page(homestayPage.getNumber())
                .size(homestayPage.getSize())
                .total(homestayPage.getTotalElements())
                .items(summaries)
                .build();

        return new ApiResponse<>(200, "Homestays retrieved successfully", pageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HomestayDTO> getHomestayDetailForAdmin(Long adminUserId, Long homestayId) {
        if (adminUserId == null) {
            throw new IllegalArgumentException("Admin user id is required");
        }

        Admin admin = adminRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for user id " + adminUserId));

        if (admin.getStatus() != Status.ACTIVE) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + homestayId));

        List<HomestayImage> images = homestayImageRepository.findByHomestay(homestay);
        HomestayDTO response = homestayMapper.toDto(homestay, images);
        return new ApiResponse<>(200, "Homestay detail retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<HomestayDTO> approveHomestay(Long adminUserId, Long homestayId) {
        if (adminUserId == null) {
            throw new IllegalArgumentException("Admin user id is required");
        }

        Admin admin = adminRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for user id " + adminUserId));

        if (admin.getStatus() != Status.ACTIVE) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + homestayId));

        if (homestay.getStatusHomestay() != StatusHomestay.PENDING) {
            return new ApiResponse<>(409, "Homestay is not in a pending state", null);
        }

        homestay.setStatusHomestay(StatusHomestay.ACTIVE);
        Homestay savedHomestay = homestayRepository.save(homestay);

        List<HomestayImage> images = homestayImageRepository.findByHomestay(savedHomestay);
        HomestayDTO response = homestayMapper.toDto(savedHomestay, images);

        return new ApiResponse<>(200, "Homestay approved successfully", response);
    }

    private void validateRequest(HomestayCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request payload is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Homestay title is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Homestay description is required");
        }
        if (request.getAddress() == null) {
            throw new IllegalArgumentException("Homestay address is required");
        }
        if (request.getMinGuest() != null && request.getMaxGuest() != null
                && request.getMinGuest() > request.getMaxGuest()) {
            throw new IllegalArgumentException("Minimum guests cannot exceed maximum guests");
        }
        if (request.getImageUrls() != null) {
            boolean invalidImage = request.getImageUrls().stream()
                    .anyMatch(image -> image == null
                            || image.getImageUrl() == null
                            || image.getImageUrl().isBlank());
            if (invalidImage) {
                throw new IllegalArgumentException("Each image entry must include a non-empty imageUrl");
            }
        }
    }

    private void applyAddress(Homestay homestay, HomestayCreateRequest request) {
        var addressRequest = request.getAddress();
        Address address = Address.builder()
                .addressLine(addressRequest.getAddressLine())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .build();
        address.setHomestay(homestay);
        homestay.setAddress(address);
    }

    private void applyFacilities(Homestay homestay, HomestayCreateRequest request) {
        List<Long> facilitiesIds = request.getFacilitiesIds();
        if (facilitiesIds == null || facilitiesIds.isEmpty()) {
            homestay.setListFacilities(new HashSet<>());
            return;
        }

        List<Facilities> facilities = facilitiesRepository.findByIdIn(facilitiesIds);
        if (facilities.size() != facilitiesIds.size()) {
            Set<Long> foundIds = facilities.stream()
                    .map(Facilities::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<Long> missingIds = facilitiesIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new IllegalArgumentException("Facilities not found for ids: " + missingIds);
        }
        homestay.setListFacilities(new HashSet<>(facilities));
    }

    private void applyAmenities(Homestay homestay, HomestayCreateRequest request) {
        if (request.getAmenities() == null || request.getAmenities().isEmpty()) {
            homestay.setListAmenities(new HashSet<>());
            return;
        }

        List<Amenities> amenitiesToPersist = request.getAmenities().stream()
                .map(amenityRequest -> Amenities.builder()
                        .name(amenityRequest.getName())
                        .description(amenityRequest.getDescription())
                        .imageUrl(amenityRequest.getImageUrl())
                        .build())
                .toList();

        List<Amenities> amenities = amenitiesRepository.saveAll(amenitiesToPersist);

        homestay.setListAmenities(new HashSet<>(amenities));
    }

    private void applyRules(Homestay homestay, HomestayCreateRequest request) {
        List<HomestayRuleRequest> ruleRequests = request.getRules();
        if (ruleRequests == null || ruleRequests.isEmpty()) {
            homestay.setListHomestayRule(new HashSet<>());
            return;
        }
        Set<HomestayRule> rules = ruleRequests.stream()
                .map(ruleRequest -> HomestayRule.builder()
                        .description(ruleRequest.getDescription())
                        .ruleTypeHomestay(ruleRequest.getRuleTypeHomestay())
                        .homestay(homestay)
                        .build())
                .collect(Collectors.toSet());
        homestay.setListHomestayRule(rules);
    }

    private void applyDailyPrices(Homestay homestay, HomestayCreateRequest request) {
        List<HomestayDailyPriceRequest> priceRequests = request.getDailyPrices();
        if (priceRequests == null || priceRequests.isEmpty()) {
            homestay.setListHomestayDailyPrice(new HashSet<>());
            return;
        }

        Set<HomestayDailyPrice> dailyPrices = priceRequests.stream()
                .map(priceRequest -> {
                    PricePerDay pricePerDay = resolvePricePerDay(priceRequest.getDay(), priceRequest.getPrice());
                    return HomestayDailyPrice.builder()
                            .price(priceRequest.getPrice())
                            .isBooked(Boolean.FALSE)
                            .pricePerDay(pricePerDay)
                            .homestay(homestay)
                            .build();
                })
                .collect(Collectors.toSet());

        homestay.setListHomestayDailyPrice(dailyPrices);
    }

    private PricePerDay resolvePricePerDay(Date day, Float price) {
        if (day == null) {
            throw new IllegalArgumentException("Daily price requires a valid day");
        }
        Optional<PricePerDay> existing = pricePerDayRepository.findByDay(day);
        if (existing.isPresent()) {
            return existing.get();
        }
        PricePerDay newPricePerDay = PricePerDay.builder()
                .day(day)
                .price(price)
                .build();
        return pricePerDayRepository.save(newPricePerDay);
    }

    private List<HomestayImage> persistImages(Homestay homestay, List<HomestayImage> imagesPayload) {
        if (imagesPayload == null || imagesPayload.isEmpty()) {
            return List.of();
        }

        List<HomestayImage> images = new ArrayList<>();
        boolean hasPrimary = imagesPayload.stream()
                .anyMatch(image -> Boolean.TRUE.equals(image.getIsPrimary()));

        for (int i = 0; i < imagesPayload.size(); i++) {
            HomestayImage payloadImage = imagesPayload.get(i);
            boolean isPrimary = Boolean.TRUE.equals(payloadImage.getIsPrimary());
            if (!hasPrimary && i == 0) {
                isPrimary = true;
            }

            HomestayImage image = HomestayImage.builder()
                    .imageUrl(payloadImage.getImageUrl())
                    .isPrimary(isPrimary)
                    .homestay(homestay)
                    .build();
            images.add(image);
        }

        return homestayImageRepository.saveAll(images);
    }

    @Override
    public ApiResponse<?> findHomestay(FindHomeStayDTO findHomeStayDTO) {
        List<Homestay> homestays = homestayRepository.findHomestay(
                findHomeStayDTO.getAddress(),
                findHomeStayDTO.getNumberChildren(),
                findHomeStayDTO.getNumberAdults() ,
                findHomeStayDTO.getNumberBaby(),
                findHomeStayDTO.getBegin(),
                findHomeStayDTO.getEnd());

        return new ApiResponse<>(200, "Success", homestays);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public ApiResponse<?> getAll(int page, int size) {
//        // Validate và chuẩn hóa page, size
//        int safePage = Math.max(page, 0);
//        int safeSize = size > 0 && size <= 100 ? size : 20;
//
//        // Lấy danh sách homestay với status ACTIVE, sort theo createdAt (mới nhất trước)
//        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
//        Page<Homestay> homestayPage = homestayRepository.findByStatusHomestay(StatusHomestay.ACTIVE, pageable);
//
//        // Map sang DTO với images
//        List<HomestayDTO> homestayDTOS = homestayPage.getContent().stream()
//                .map(homestay -> {
//                    List<HomestayImage> images = homestayImageRepository.findByHomestay(homestay);
//                    return homestayMapper.toDto(homestay, images);
//                })
//                .collect(Collectors.toList());
//
//        // Tạo PageResponse
//        PageResponse<List<HomestayDTO>> pageResponse = PageResponse.<List<HomestayDTO>>builder()
//                .page(homestayPage.getNumber())
//                .size(homestayPage.getSize())
//                .total(homestayPage.getTotalElements())
//                .items(homestayDTOS)
//                .build();
//
//        return new ApiResponse<>(200, "Homestays retrieved successfully", pageResponse);
//    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> findUserHistoryHomestays(Long userId) {
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
                .filter(bill -> {
                    StatusBill status = bill.getStatus();
                    return status == StatusBill.SUCCEED 
//                            || status == StatusBill.COMPLAINT_EXPIRED
                            || status == StatusBill.CHECKIN_EXPIRED
                            || status == StatusBill.COMPLAINT_PENDING
                            || status == StatusBill.CHECKIN_PENDING;
                })
                .collect(Collectors.toList());

        // Map sang BillDTO
        List<BillDTO> billDTOS = completedBills.stream()
                .map(BillMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "User booking history retrieved successfully", billDTOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> detailHomestay(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Homestay id is required");
        }

        // Lấy homestay theo id
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + id));

        // Chỉ trả về homestay có status ACTIVE cho public
        if (homestay.getStatusHomestay() != StatusHomestay.ACTIVE) {
            return new ApiResponse<>(404, "Homestay is not available", null);
        }

        // Lấy images của homestay
        List<HomestayImage> images = homestayImageRepository.findByHomestay(homestay);

        // Map sang HomestayDTO đầy đủ
        HomestayDTO homestayDTO = homestayMapper.toDto(homestay, images);

        return new ApiResponse<>(200, "Homestay detail retrieved successfully", homestayDTO);
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

    @Override
    @Transactional
    public ApiResponse<HomestayDTO> updateHomestayPrices(Long homestayId, UpdateHomestayPriceRequest request) {
        if (homestayId == null) {
            throw new IllegalArgumentException("Homestay ID is required");
        }
        if (request == null || request.getDailyPrices() == null || request.getDailyPrices().isEmpty()) {
            throw new IllegalArgumentException("Daily prices list is required");
        }

        // Lấy homestay
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id: " + homestayId));

        // Xử lý từng daily price trong request
        for (UpdateHomestayPriceRequest.DailyPriceUpdate priceUpdate : request.getDailyPrices()) {
            if (priceUpdate.getDay() == null || priceUpdate.getPrice() == null) {
                continue; // Bỏ qua nếu thiếu thông tin
            }

            // Tìm hoặc tạo PricePerDay
            PricePerDay pricePerDay = resolvePricePerDay(priceUpdate.getDay(), priceUpdate.getPrice());

            // Tìm HomestayDailyPrice hiện có cho homestay và pricePerDay này
            Optional<HomestayDailyPrice> existingDailyPrice = homestay.getListHomestayDailyPrice().stream()
                    .filter(hdp -> hdp.getPricePerDay() != null && 
                            hdp.getPricePerDay().getId().equals(pricePerDay.getId()))
                    .findFirst();

            if (existingDailyPrice.isPresent()) {
                // Cập nhật giá nếu đã tồn tại
                HomestayDailyPrice dailyPrice = existingDailyPrice.get();
                // Chỉ cập nhật nếu chưa được booked
                if (!Boolean.TRUE.equals(dailyPrice.getIsBooked())) {
                    dailyPrice.setPrice(priceUpdate.getPrice());
                    homestayDailyPricesRepository.save(dailyPrice);
                }
            } else {
                // Tạo mới HomestayDailyPrice
                HomestayDailyPrice newDailyPrice = HomestayDailyPrice.builder()
                        .price(priceUpdate.getPrice())
                        .isBooked(Boolean.FALSE)
                        .pricePerDay(pricePerDay)
                        .homestay(homestay)
                        .build();
                homestayDailyPricesRepository.save(newDailyPrice);
                
                // Thêm vào set của homestay
                if (homestay.getListHomestayDailyPrice() == null) {
                    homestay.setListHomestayDailyPrice(new HashSet<>());
                }
                homestay.getListHomestayDailyPrice().add(newDailyPrice);
            }
        }

        // Lưu homestay
        Homestay savedHomestay = homestayRepository.save(homestay);

        // Lấy images
        List<HomestayImage> images = homestayImageRepository.findByHomestay(savedHomestay);

        // Map sang DTO
        HomestayDTO responseDTO = homestayMapper.toDto(savedHomestay, images);

        return new ApiResponse<>(200, "Homestay prices updated successfully", responseDTO);
    }
}
