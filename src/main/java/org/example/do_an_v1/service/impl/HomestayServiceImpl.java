package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.dto.request.HomestayDailyPriceRequest;
import org.example.do_an_v1.dto.request.HomestayRuleRequest;
import org.example.do_an_v1.entity.Address;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.Amenities;
import org.example.do_an_v1.entity.Facilities;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.example.do_an_v1.entity.HomestayImage;
import org.example.do_an_v1.entity.HomestayRule;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.PricePerDay;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.mapper.HomestayMapper;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.AmenitiesRepository;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.FacilitiesRepository;
import org.example.do_an_v1.repository.HomestayImageRepository;
import org.example.do_an_v1.repository.HomestayRepository;
import org.example.do_an_v1.repository.HostRepository;
import org.example.do_an_v1.repository.PricePerDayRepository;
import org.example.do_an_v1.service.HomestayService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Override
    @Transactional
    public ApiResponse<HomestayDTO> createHomestay(Long hostUserId, HomestayCreateRequest request) {
        validateRequest(request);

        Host host = hostRepository.findById(hostUserId)
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
    public ApiResponse<PageResponse<List<HomestaySummaryDTO>>> getHomestaysForAdmin(Long adminUserId,
                                                                                   StatusHomestay status,
                                                                                   int page,
                                                                                   int size) {
        if (adminUserId == null) {
            throw new IllegalArgumentException("Admin user id is required");
        }

        Admin admin = adminRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for user id " + adminUserId));

        if (admin.getStatus() != Status.ACTIVE) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

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
                .address_line(addressRequest.getAddressLine())
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
                    PricePerDay pricePerDay = resolvePricePerDay(priceRequest.getDay());
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

    private PricePerDay resolvePricePerDay(java.util.Date day) {
        if (day == null) {
            throw new IllegalArgumentException("Daily price requires a valid day");
        }
        Optional<PricePerDay> existing = pricePerDayRepository.findByDay(day);
        if (existing.isPresent()) {
            return existing.get();
        }
        PricePerDay newPricePerDay = PricePerDay.builder()
                .day(day)
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
}
