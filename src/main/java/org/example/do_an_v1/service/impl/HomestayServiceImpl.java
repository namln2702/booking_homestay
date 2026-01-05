package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdvancedHomestaySearchDTO;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HomestayDetailDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.ImageDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.dto.request.HomestayDailyPriceRequest;
import org.example.do_an_v1.dto.request.HomestayRuleRequest;
import org.example.do_an_v1.dto.request.PersonCapacityRequest;
import org.example.do_an_v1.dto.request.TouristAttractionsRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayPriceRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.enums.TypePerson;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
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
    private final AdminRepository adminRepository;
    private final HomestayMapper homestayMapper;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final TouristAttractionsRepository touristAttractionsRepository;

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
        applyPersonCapacities(homestay, request);
        applyTouristAttractions(homestay, request);

        Homestay savedHomestay = homestayRepository.save(homestay);

        // Tự động tạo 30 ngày giá từ ngày tạo homestay với giá basePrice
//        generateDefaultDailyPrices(savedHomestay);

        List<Image> savedImages = persistImages(savedHomestay, request.getImageUrls());

        HomestayDTO response = homestayMapper.toDto(savedHomestay, savedImages);
        return new ApiResponse<>(201, "Homestay created successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<HomestayDTO>>> getHomestays(
            StatusHomestay status,
            int page,
            int size,
            Float minPrice,
            Float maxPrice,
            Integer minBedrooms,
            Integer minBathrooms,
            Integer minGuests,
            String city,
            String category,
            String search
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;

        Pageable pageable = PageRequest.of(safePage, safeSize);
        
        // Convert enum sang string cho native query
        String statusStr = status != null ? status.name() : null;
        
        // Sử dụng query method mới với tất cả các filter
        Page<Homestay> homestayPage = homestayRepository.findHomestaysWithFilters(
                statusStr,
                minPrice,
                maxPrice,
                minBedrooms,
                minBathrooms,
                minGuests,
                city,
                category,
                search,
                pageable
        );

        // Map từng homestay sang HomestayDTO với images
        List<HomestayDTO> homestayDTOs = homestayPage.getContent().stream()
                .map(homestay -> {
                    List<Image> images = homestay.getListImage() != null 
                            ? new ArrayList<>(homestay.getListImage()) 
                            : new ArrayList<>();
                    return homestayMapper.toDto(homestay, images);
                })
                .toList();

        PageResponse<List<HomestayDTO>> pageResponse = PageResponse.<List<HomestayDTO>>builder()
                .page(homestayPage.getNumber())
                .size(homestayPage.getSize())
                .total(homestayPage.getTotalElements())
                .active(homestayRepository.countByStatusHomestay(StatusHomestay.ACTIVE))
                .inactive(homestayRepository.countByStatusHomestay(StatusHomestay.INACTIVE))
                .pending(homestayRepository.countByStatusHomestay(StatusHomestay.PENDING))
                .items(homestayDTOs)
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

        // Sử dụng images từ homestay.getListImage() (đã được eager load nếu cần)
        List<Image> images = homestay.getListImage() != null ? new ArrayList<>(homestay.getListImage()) : new ArrayList<>();
        HomestayDTO response = homestayMapper.toDto(homestay, images);
        return new ApiResponse<>(200, "Homestay detail retrieved successfully", response);
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
        if (request.getListPersonHomestay() == null || request.getListPersonHomestay().isEmpty()) {
            throw new IllegalArgumentException("listPersonHomestay is required");
        }
        boolean invalidPersonEntry = request.getListPersonHomestay().stream()
                .anyMatch(entry ->
                        entry == null
                                || entry.getType() == null
                                || entry.getQuantity() == null
                                || entry.getQuantity() < 0
                );
        if (invalidPersonEntry) {
            throw new IllegalArgumentException("Each entry in listPersonHomestay must have type and non-negative quantity");
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
                .url(addressRequest.getUrl())
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

        List<Facilities> facilities = facilitiesRepository.findByIdInAndDeletedFalse(facilitiesIds);
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
                        .deleted(false)
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
                .map(ruleRequest -> {
                    HomestayRule rule = new HomestayRule();
                    rule.setDescription(ruleRequest.getDescription());
                    rule.setRuleTypeHomestay(ruleRequest.getRuleTypeHomestay());
                    rule.setHomestay(homestay);
                    return rule;
                })
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
                            .activeHost(false)
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

    private List<Image> persistImages(Homestay homestay, List<org.example.do_an_v1.dto.request.ImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return List.of();
        }

        List<Image> images = new ArrayList<>();
        boolean hasPrimary = imageRequests.stream()
                .anyMatch(image -> Boolean.TRUE.equals(image.getIsPrimary()));

        for (int i = 0; i < imageRequests.size(); i++) {
            org.example.do_an_v1.dto.request.ImageRequest imageRequest = imageRequests.get(i);
            boolean isPrimary = Boolean.TRUE.equals(imageRequest.getIsPrimary());
            if (!hasPrimary && i == 0) {
                isPrimary = true;
            }

            Image image = Image.builder()
                    .image_url(imageRequest.getImageUrl())
                    .isPrimary(isPrimary)
                    .homestay(homestay)
                    .build();
            images.add(image);
        }

        return imageRepository.saveAll(images);
    }

    private void applyPersonCapacities(Homestay homestay, HomestayCreateRequest request) {
        List<PersonCapacityRequest> personRequests = request.getListPersonHomestay();
        if (personRequests == null || personRequests.isEmpty()) {
            throw new IllegalArgumentException("listPersonHomestay is required");
        }

        Set<PersonHomestay> capacities = personRequests.stream()
                .map(req -> {
                    if (req == null || req.getType() == null) {
                        throw new IllegalArgumentException("Each entry in listPersonHomestay must have a type");
                    }
                    if (req.getQuantity() == null || req.getQuantity() < 0) {
                        throw new IllegalArgumentException("Quantity for " + req.getType() + " must be >= 0");
                    }
                    Person person = resolvePerson(req.getType());
                    return PersonHomestay.builder()
                            .homestay(homestay)
                            .person(person)
                            .quantity(req.getQuantity())
                            .build();
                })
                .collect(Collectors.toSet());

        homestay.setListPersonHomestay(capacities);
    }

    private void applyTouristAttractions(Homestay homestay, HomestayCreateRequest request) {
        List<TouristAttractionsRequest> touristAttractionsRequests = request.getTouristAttractions();
        if (touristAttractionsRequests == null || touristAttractionsRequests.isEmpty()) {
            return;
        }

        List<TouristAttractions> touristAttractions = touristAttractionsRequests.stream()
                .map(req -> {
                    if (req == null || req.getName() == null || req.getName().isBlank()) {
                        throw new IllegalArgumentException("Tourist attraction name is required");
                    }
                    if (req.getDescription() == null || req.getDescription().isBlank()) {
                        throw new IllegalArgumentException("Tourist attraction description is required");
                    }
                    if (req.getImageUrl() == null || req.getImageUrl().isBlank()) {
                        throw new IllegalArgumentException("Tourist attraction image URL is required");
                    }
                    return TouristAttractions.builder()
                            .name(req.getName())
                            .description(req.getDescription())
                            .imageUrl(req.getImageUrl())
                            .deleted(false)
                            .homestay(homestay)
                            .build();
                })
                .collect(Collectors.toList());

        touristAttractionsRepository.saveAll(touristAttractions);
    }

    private Person resolvePerson(TypePerson type) {
        return personRepository.findByType(type)
                .orElseGet(() -> personRepository.save(Person.builder().type(type).build()));
    }

    @Override
    public ApiResponse<?> findHomestay(FindHomeStayDTO findHomeStayDTO) {
        // Xử lý city: trim và chuyển empty thành null
        String city = (findHomeStayDTO.getCity() != null && !findHomeStayDTO.getCity().trim().isEmpty()) 
                ? findHomeStayDTO.getCity().trim() : null;
        
        // Xử lý state: trim và chuyển empty thành null
        String state = (findHomeStayDTO.getState() != null && !findHomeStayDTO.getState().trim().isEmpty()) 
                ? findHomeStayDTO.getState().trim() : null;
        
        // Xử lý số người: giữ nguyên giá trị hoặc null nếu không truyền
        Integer numAdults = findHomeStayDTO.getNumberAdults();
        Integer numChildren = findHomeStayDTO.getNumberChildren();
        Integer numBaby = findHomeStayDTO.getNumberBaby();
        
        // Xử lý ngày tháng: giữ nguyên hoặc null
        Date begin = parseDate(findHomeStayDTO.getBegin());
        Date end = parseDate(findHomeStayDTO.getEnd());

        List<Homestay> homestays;
        if (begin != null && end != null) {
            if (begin.after(end)) {
                throw new IllegalArgumentException("Begin date must be before or equal to end date");
            }
            homestays = homestayRepository.findHomestayWithDateRange(
                    city,
                    state,
                    numAdults,
                    numChildren,
                    numBaby,
                    begin,
                    end
            );
        } else {
            homestays = homestayRepository.findHomestay(
                    city,
                    state,
                    numAdults,
                    numChildren,
                    numBaby
            );
        }

        List<HomestayDTO> homestayDTOS = homestays.stream()
                .map(homestay -> {
                    List<Image> images = homestay.getListImage() != null ? new ArrayList<>(homestay.getListImage()) : new ArrayList<>();
                    return homestayMapper.toDto(homestay, images);
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Success", homestayDTOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> searchHomestayAdvanced(AdvancedHomestaySearchDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Search payload is required");
        }

        String keyword = normalizeSearchInput(request.getKeyword());
        String city = normalizeSearchInput(request.getCity());
        String state = normalizeSearchInput(request.getState());

        // JDBC native queries cannot bind null for unknown types, use empty strings as "not provided"
        String keywordParam = keyword == null ? "" : keyword;
        String cityParam = city == null ? "" : city;
        String stateParam = state == null ? "" : state;

        Integer numAdults = request.getNumberAdults();
        Integer numChildren = request.getNumberChildren();
        Integer numBaby = request.getNumberBaby();

        Date begin = parseDate(request.getBegin());
        Date end = parseDate(request.getEnd());

        if (begin != null && end != null && begin.after(end)) {
            throw new IllegalArgumentException("Begin date must be before or equal to end date");
        }

        List<Homestay> homestays = homestayRepository.searchHomestayAdvanced(
                keywordParam,
                cityParam,
                stateParam,
                numAdults,
                numChildren,
                numBaby,
                begin,
                end
        );

        List<HomestayDTO> results = homestays.stream()
                .map(homestay -> {
                    List<Image> images = homestay.getListImage() != null ? new ArrayList<>(homestay.getListImage()) : new ArrayList<>();
                    return homestayMapper.toDto(homestay, images);
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Success", results);
    }

    private Date parseDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(rawDate.trim());
            return java.sql.Date.valueOf(localDate);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd");
        }
    }

    private String normalizeSearchInput(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase();
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
//                    List<Image> images = homestay.getListImage() != null ? new ArrayList<>(homestay.getListImage()) : new ArrayList<>();
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
    public ApiResponse<?> detailHomestay(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Homestay id is required");
        }

        // Lấy homestay theo id
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + id));

        // Chỉ trả về homestay có status ACTIVE cho public
//        if (homestay.getStatusHomestay() != StatusHomestay.ACTIVE) {
//            return new ApiResponse<>(404, "Homestay is not available", null);
//        }

        // Lấy images của homestay từ entity
        List<Image> images = homestay.getListImage() != null ? new ArrayList<>(homestay.getListImage()) : new ArrayList<>();

        // Map sang HomestayDTO đầy đủ
        HomestayDTO homestayDTO = homestayMapper.toDto(homestay, images);

        return new ApiResponse<>(200, "Homestay detail retrieved successfully", homestayDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HomestayDetailDTO> detailHomestayFull(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Homestay id is required");
        }

        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + id));

        List<Image> images = homestay.getListImage() != null ? new ArrayList<>(homestay.getListImage()) : new ArrayList<>();
        HomestayDTO baseDto = homestayMapper.toDto(homestay, images);
        List<Review> reviews = reviewRepository.findByHomestay(homestay);

        HomestayDetailDTO detailDTO = HomestayDetailDTO.builder()
                .id(homestay.getId())
                .title(homestay.getTitle())
                .description(homestay.getDescription())
                .category(homestay.getCategory())
                .rating(homestay.getRating())
                .minGuest(homestay.getMinGuest())
                .maxGuest(homestay.getMaxGuest())
                .numBedrooms(homestay.getNumBedrooms())
                .numBeds(homestay.getNumBeds())
                .numBathrooms(homestay.getNumBathrooms())
                .numKitchen(homestay.getNumKitchen())
                .advancedPayment(homestay.getAdvancedPayment())
                .warningCount(homestay.getWarningCount())
                .basePrice(homestay.getBasePrice())
                .status(homestay.getStatusHomestay())
                .address(baseDto.getAddress())
                .facilities(baseDto.getFacilities())
                .amenities(baseDto.getAmenities())
                .rules(baseDto.getRules())
                .dailyPrices(baseDto.getDailyPrices())
                .images(baseDto.getImages())
                .personCapacities(baseDto.getPersonCapacities())
                .host(mapHostSummary(homestay.getHost()))
                .priceInsight(buildPriceInsight(homestay))
                .reviews(mapReviewDetails(reviews))
                .build();

        return new ApiResponse<>(200, "Homestay detail retrieved successfully", detailDTO);
    }

    private HomestayDetailDTO.HostSummaryDTO mapHostSummary(Host host) {
        if (host == null) {
            return null;
        }
        User user = host.getUser();
        return HomestayDetailDTO.HostSummaryDTO.builder()
                .hostId(host.getId())
                .status(host.getStatusHost())
                .businessName(host.getBusinessName())
                .qrCodeUrl(host.getQrCodeUrl())
                .userId(user != null ? user.getId() : null)
                .fullName(user != null ? user.getName() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .phone(user != null ? user.getPhone() : null)
                .email(user != null ? user.getEmail() : null)
                .build();
    }

    private HomestayDetailDTO.PriceInsightDTO buildPriceInsight(Homestay homestay) {
        Set<HomestayDailyPrice> priceSet = homestay.getListHomestayDailyPrice();
        Float basePrice = homestay.getBasePrice();
        if (priceSet == null || priceSet.isEmpty()) {
            return HomestayDetailDTO.PriceInsightDTO.builder()
                    .minPrice(basePrice)
                    .maxPrice(basePrice)
                    .averagePrice(basePrice)
                    .totalNights(0)
                    .availableNights(0)
                    .bookedNights(0)
                    .build();
        }

        List<HomestayDailyPrice> sorted = priceSet.stream()
                .filter(price -> price.getPricePerDay() != null && price.getPricePerDay().getDay() != null)
                .sorted(Comparator.comparing(price -> price.getPricePerDay().getDay()))
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            int available = (int) priceSet.stream()
                    .filter(price -> !Boolean.TRUE.equals(price.getIsBooked()))
                    .count();
            int booked = priceSet.size() - available;
            return HomestayDetailDTO.PriceInsightDTO.builder()
                    .minPrice(basePrice)
                    .maxPrice(basePrice)
                    .averagePrice(basePrice)
                    .totalNights(priceSet.size())
                    .availableNights(available)
                    .bookedNights(booked)
                    .build();
        }

        Float minPrice = sorted.stream()
                .map(HomestayDailyPrice::getPrice)
                .filter(Objects::nonNull)
                .min(Float::compare)
                .orElse(basePrice);

        Float maxPrice = sorted.stream()
                .map(HomestayDailyPrice::getPrice)
                .filter(Objects::nonNull)
                .max(Float::compare)
                .orElse(basePrice);

        OptionalDouble avgOptional = sorted.stream()
                .map(HomestayDailyPrice::getPrice)
                .filter(Objects::nonNull)
                .mapToDouble(Float::doubleValue)
                .average();
        Float averagePrice = avgOptional.isPresent() ? (float) avgOptional.getAsDouble() : basePrice;

        long available = sorted.stream()
                .filter(price -> !Boolean.TRUE.equals(price.getIsBooked()))
                .count();
        long booked = sorted.size() - available;

        LocalDate firstDate = toLocalDate(sorted.get(0).getPricePerDay().getDay());
        LocalDate lastDate = toLocalDate(sorted.get(sorted.size() - 1).getPricePerDay().getDay());

        return HomestayDetailDTO.PriceInsightDTO.builder()
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .averagePrice(averagePrice)
                .totalNights(sorted.size())
                .availableNights((int) available)
                .bookedNights((int) booked)
                .firstDate(firstDate)
                .lastDate(lastDate)
                .build();
    }

    private List<HomestayDetailDTO.ReviewDetailDTO> mapReviewDetails(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return List.of();
        }
        return reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt,
                        Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder())).reversed())
                .map(review -> {
                    List<ImageDTO> reviewImages = review.getListImage() == null
                            ? List.of()
                            : review.getListImage().stream()
                            .filter(Objects::nonNull)
                            .map(ImageMapper::toDTO)
                            .collect(Collectors.toList());

                    return HomestayDetailDTO.ReviewDetailDTO.builder()
                            .id(review.getId())
                            .rating(review.getRating())
                            .comment(review.getComment())
                            .createdAt(review.getCreatedAt())
                            .reviewer(mapReviewer(review.getCustomer()))
                            .images(reviewImages)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private HomestayDetailDTO.ReviewerDTO mapReviewer(Customer customer) {
        if (customer == null) {
            return null;
        }
        User user = customer.getUser();
        return HomestayDetailDTO.ReviewerDTO.builder()
                .id(customer.getId())
                .name(user != null ? user.getName() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .lastBooking(customer.getLastBooking())
                .build();
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

//    @Override
//    @Transactional
//    public ApiResponse<?> reviewHomestay(Long userId, ReviewDTO reviewDTO) {
//        if (userId == null) {
//            throw new IllegalArgumentException("User id is required");
//        }
//        if (reviewDTO == null) {
//            throw new IllegalArgumentException("Review data is required");
//        }
//        if (reviewDTO.getHomestayId() == null) {
//            throw new IllegalArgumentException("Homestay id is required");
//        }
//        if (reviewDTO.getRating() == null || reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
//            throw new IllegalArgumentException("Rating must be between 1 and 5");
//        }
//        if (reviewDTO.getComment() == null || reviewDTO.getComment().trim().isEmpty()) {
//            throw new IllegalArgumentException("Comment is required");
//        }
//
//        // Lấy user và customer
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));
//
//        Customer customer = customerRepository.findByUser(user);
//        if (customer == null) {
//            return new ApiResponse<>(404, "Customer profile not found for this user", null);
//        }
//
//        // Lấy homestay
//        Homestay homestay = homestayRepository.findById(reviewDTO.getHomestayId())
//                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id " + reviewDTO.getHomestayId()));
//
//        // Validate: Customer đã booking homestay này chưa?
//        Optional<Bill> billOptional = billRepository.findByHomestayAndCustomer(homestay, customer);
//        if (billOptional.isEmpty()) {
//            return new ApiResponse<>(403, "You must book this homestay before reviewing", null);
//        }
//
//        Bill bill = billOptional.get();
//        // Chỉ cho phép review nếu đã check-in hoặc đã hoàn tất
//        StatusBill billStatus = bill.getStatus();
//        if (billStatus != StatusBill.COMPLAINT_PENDING
//                && billStatus != StatusBill.CHECKIN_PENDING
////                && billStatus != StatusBill.COMPLAINT_EXPIRED
//                && billStatus != StatusBill.SUCCEED) {
//            return new ApiResponse<>(403, "You can only review homestays you have stayed at", null);
//        }
//
//        // Validate: Customer đã review homestay này chưa? (tránh duplicate)
//        Optional<Review> existingReview = reviewRepository.findByHomestayAndCustomer(homestay, customer);
//        if (existingReview.isPresent()) {
//            return new ApiResponse<>(409, "You have already reviewed this homestay", null);
//        }
//
//        // Tạo Review entity
//        Review review = Review.builder()
//                .rating(reviewDTO.getRating())
//                .comment(reviewDTO.getComment().trim())
//                .homestay(homestay)
//                .customer(customer)
//                .build();
//
//        // Lưu review trước để có ID
//        Review savedReview = reviewRepository.save(review);
//
//        // Xử lý images nếu có
//        Set<Image> reviewImages = new HashSet<>();
//        if (reviewDTO.getImageUrls() != null && !reviewDTO.getImageUrls().isEmpty()) {
//            final Review reviewForImages = savedReview; // Make final for lambda
//            List<Image> imagesToSave = reviewDTO.getImageUrls().stream()
//                    .filter(img -> img != null && img.getImage_url() != null && !img.getImage_url().trim().isEmpty())
//                    .map(img -> {
//                        Image image = ImageMapper.toEntity(img);
//                        image.setReview(reviewForImages);
//                        return imageRepository.save(image);
//                    })
//                    .collect(Collectors.toList());
//            reviewImages.addAll(imagesToSave);
//            savedReview.setListImage(reviewImages);
//            savedReview = reviewRepository.save(savedReview);
//        }
//
//        // Sử dụng savedReview làm finalReview
//        final Review finalReview = savedReview;
//
//        // Thêm review vào homestay
//        Set<Review> reviews = homestay.getListReview();
//        if (reviews == null) {
//            reviews = new HashSet<>();
//        }
//        reviews.add(finalReview);
//        homestay.setListReview(reviews);
//
//        // Tính lại rating trung bình của homestay
//        List<Review> allReviews = reviewRepository.findByHomestay(homestay);
//        if (!allReviews.isEmpty()) {
//            double averageRating = allReviews.stream()
//                    .mapToInt(Review::getRating)
//                    .average()
//                    .orElse(0.0);
//            homestay.setRating((float) averageRating);
//        }
//
//        // Lưu homestay với rating mới
//        homestay = homestayRepository.save(homestay);
//
//        // Map sang DTO để trả về
//        ReviewDTO responseDTO = ReviewMapper.toDTO(finalReview);
//
//        return new ApiResponse<>(201, "Review submitted successfully", responseDTO);
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<?> updateReviewHomestay(Long userId, Long reviewId, ReviewDTO reviewDTO) {
//        if (userId == null) {
//            throw new IllegalArgumentException("User id is required");
//        }
//        if (reviewId == null) {
//            throw new IllegalArgumentException("Review id is required");
//        }
//        if (reviewDTO == null) {
//            throw new IllegalArgumentException("Review data is required");
//        }
//        if (reviewDTO.getRating() != null && (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5)) {
//            throw new IllegalArgumentException("Rating must be between 1 and 5");
//        }
//        if (reviewDTO.getComment() != null && reviewDTO.getComment().trim().isEmpty()) {
//            throw new IllegalArgumentException("Comment cannot be empty");
//        }
//
//        // Lấy user và customer
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));
//
//        Customer customer = customerRepository.findByUser(user);
//        if (customer == null) {
//            return new ApiResponse<>(404, "Customer profile not found for this user", null);
//        }
//
//        // Tìm review theo ID
//        Review review = reviewRepository.findById(reviewId)
//                .orElseThrow(() -> new IllegalArgumentException("Review not found for id " + reviewId));
//
//        // Validate: Review phải thuộc về customer hiện tại
//        if (!review.getCustomer().getId().equals(customer.getId())) {
//            return new ApiResponse<>(403, "You can only update your own reviews", null);
//        }
//
//        // Lấy homestay từ review
//        Homestay homestay = review.getHomestay();
//        if (homestay == null) {
//            return new ApiResponse<>(404, "Homestay not found for this review", null);
//        }
//
//        // Update rating nếu có
//        if (reviewDTO.getRating() != null) {
//            review.setRating(reviewDTO.getRating());
//        }
//
//        // Update comment nếu có
//        if (reviewDTO.getComment() != null) {
//            review.setComment(reviewDTO.getComment().trim());
//        }
//
//        // Xử lý images: xóa images cũ và thêm images mới
//        if (reviewDTO.getImageUrls() != null) {
//            // Xóa tất cả images cũ của review
//            Set<Image> oldImages = review.getListImage();
//            if (oldImages != null && !oldImages.isEmpty()) {
//                // Set review = null cho các images cũ trước khi xóa
//                oldImages.forEach(image -> image.setReview(null));
//                imageRepository.deleteAll(oldImages);
//            }
//
//            // Thêm images mới nếu có
//            Set<Image> newImages = new HashSet<>();
//            if (!reviewDTO.getImageUrls().isEmpty()) {
//                final Review reviewForImages = review; // Make final for lambda
//                List<Image> imagesToSave = reviewDTO.getImageUrls().stream()
//                        .filter(img -> img != null && img.getImage_url() != null && !img.getImage_url().trim().isEmpty())
//                        .map(img -> {
//                            Image image = ImageMapper.toEntity(img);
//                            image.setReview(reviewForImages);
//                            return imageRepository.save(image);
//                        })
//                        .collect(Collectors.toList());
//                newImages.addAll(imagesToSave);
//            }
//            review.setListImage(newImages);
//        }
//
//        // Lưu review đã update
//        Review updatedReview = reviewRepository.save(review);
//
//        // Tính lại rating trung bình của homestay
//        // List<Review> allReviews = reviewRepository.findByHomestay(homestay);
//        // if (!allReviews.isEmpty()) {
//        //     double averageRating = allReviews.stream()
//        //             .mapToInt(Review::getRating)
//        //             .average()
//        //             .orElse(0.0);
//        //     homestay.setRating((float) averageRating);
//        //     homestayRepository.save(homestay);
//        // }
//
//        // Map sang DTO để trả về
//        ReviewDTO responseDTO = ReviewMapper.toDTO(updatedReview);
//
//        return new ApiResponse<>(200, "Review updated successfully", responseDTO);
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<HomestayDTO> updateHomestayPrices(Long homestayId, UpdateHomestayPriceRequest request) {
//        if (homestayId == null) {
//            throw new IllegalArgumentException("Homestay ID is required");
//        }
//        if (request == null || request.getDailyPrices() == null || request.getDailyPrices().isEmpty()) {
//            throw new IllegalArgumentException("Daily prices list is required");
//        }
//
//        // Lấy homestay
//        Homestay homestay = homestayRepository.findById(homestayId)
//                .orElseThrow(() -> new IllegalArgumentException("Homestay not found for id: " + homestayId));
//
//        // Xử lý từng daily price trong request
//        for (UpdateHomestayPriceRequest.DailyPriceUpdate priceUpdate : request.getDailyPrices()) {
//            if (priceUpdate.getDay() == null || priceUpdate.getPrice() == null) {
//                continue; // Bỏ qua nếu thiếu thông tin
//            }
//
//            // Tìm hoặc tạo PricePerDay
//            PricePerDay pricePerDay = resolvePricePerDay(priceUpdate.getDay(), priceUpdate.getPrice());
//
//            // Tìm HomestayDailyPrice hiện có cho homestay và pricePerDay này
//            Optional<HomestayDailyPrice> existingDailyPrice = homestay.getListHomestayDailyPrice().stream()
//                    .filter(hdp -> hdp.getPricePerDay() != null &&
//                            hdp.getPricePerDay().getId().equals(pricePerDay.getId()))
//                    .findFirst();
//
//            if (existingDailyPrice.isPresent()) {
//                // Cập nhật giá nếu đã tồn tại
//                HomestayDailyPrice dailyPrice = existingDailyPrice.get();
//                // Chỉ cập nhật nếu chưa được booked
//                if (!Boolean.TRUE.equals(dailyPrice.getIsBooked())) {
//                    dailyPrice.setPrice(priceUpdate.getPrice());
//                    homestayDailyPricesRepository.save(dailyPrice);
//                }
//            } else {
//                // Tạo mới HomestayDailyPrice
//                HomestayDailyPrice newDailyPrice = HomestayDailyPrice.builder()
//                        .price(priceUpdate.getPrice())
//                        .isBooked(Boolean.FALSE)
//                        .pricePerDay(pricePerDay)
//                        .homestay(homestay)
//                        .build();
//                homestayDailyPricesRepository.save(newDailyPrice);
//
//                // Thêm vào set của homestay
//                if (homestay.getListHomestayDailyPrice() == null) {
//                    homestay.setListHomestayDailyPrice(new HashSet<>());
//                }
//                homestay.getListHomestayDailyPrice().add(newDailyPrice);
//            }
//        }
//
//        // Lưu homestay
//        Homestay savedHomestay = homestayRepository.save(homestay);
//
//        // Lấy images
//        List<Image> images = savedHomestay.getListImage() != null ? new ArrayList<>(savedHomestay.getListImage()) : new ArrayList<>();
//
//        // Map sang DTO
//        HomestayDTO responseDTO = homestayMapper.toDto(savedHomestay, images);
//
//        return new ApiResponse<>(200, "Homestay prices updated successfully", responseDTO);
//    }

    /**
     * Tự động tạo 30 ngày giá từ ngày tạo homestay với giá basePrice
     * @param homestay Homestay đã được lưu vào database
     */
//    private void generateDefaultDailyPrices(Homestay homestay) {
//        if (homestay == null || homestay.getBasePrice() == null) {
//            return;
//        }
//
//        Float basePrice = homestay.getBasePrice();
//        if (basePrice <= 0) {
//            return; // Không tạo giá nếu basePrice <= 0
//        }
//
//        // Lấy ngày tạo homestay (nếu chưa có thì dùng ngày hiện tại)
//        LocalDate startDate;
//        if (homestay.getCreatedAt() != null) {
//            startDate = homestay.getCreatedAt().toLocalDate();
//        } else {
//            startDate = LocalDate.now();
//        }
//
//        List<HomestayDailyPrice> dailyPricesToCreate = new ArrayList<>();
//
//        // Tạo 30 ngày giá từ ngày tạo homestay
//        for (int i = 0; i < 30; i++) {
//            LocalDate currentDate = startDate.plusDays(i);
//            java.util.Date dateUtil = java.sql.Date.valueOf(currentDate);
//
//            // Kiểm tra xem đã có HomestayDailyPrice cho ngày này chưa
//            Optional<HomestayDailyPrice> existingDailyPrice = homestayDailyPricesRepository
//                    .findByHomestayAndDate(homestay.getId(), dateUtil);
//
//            if (existingDailyPrice.isPresent()) {
//                // Đã có giá cho ngày này, bỏ qua
//                continue;
//            }
//
//            // Tìm hoặc tạo PricePerDay cho ngày này
//            PricePerDay pricePerDay = pricePerDayRepository.findByDay(dateUtil)
//                    .orElseGet(() -> {
//                        PricePerDay newPricePerDay = PricePerDay.builder()
//                                .day(dateUtil)
//                                .price(basePrice)
//                                .build();
//                        return pricePerDayRepository.save(newPricePerDay);
//                    });
//
//            // Tạo HomestayDailyPrice mới
//            HomestayDailyPrice dailyPrice = HomestayDailyPrice.builder()
//                    .price(basePrice)
//                    .isBooked(Boolean.FALSE)
//                    .pricePerDay(pricePerDay)
//                    .homestay(homestay)
//                    .build();
//
//            dailyPricesToCreate.add(dailyPrice);
//        }
//
//        // Lưu tất cả daily prices vào database
//        if (!dailyPricesToCreate.isEmpty()) {
//            homestayDailyPricesRepository.saveAll(dailyPricesToCreate);
//        }
//    }
}
