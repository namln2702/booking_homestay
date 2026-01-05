package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.*;
import org.example.do_an_v1.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HomestayMapper {

    public HomestayDTO toDto(Homestay homestay, List<Image> images) {
        if (homestay == null) {
            return null;
        }

        AddressDTO addressDTO = mapAddress(homestay.getAddress());

        List<HomestayFacilityDTO> facilities = mapFacilities(homestay.getListFacilities());
        List<HomestayAmenityDTO> amenities = mapAmenities(homestay.getListAmenities());
        List<HomestayRuleDTO> rules = mapRules(homestay.getListHomestayRule());
        List<HomestayDailyPriceDTO> dailyPrices = mapDailyPrices(homestay.getListHomestayDailyPrice());
        // Sử dụng images từ parameter hoặc từ homestay.getListImage()
        List<Image> imagesToMap = images != null ? images : (homestay.getListImage() != null ? new ArrayList<>(homestay.getListImage()) : new ArrayList<>());
        List<HomestayImageDTO> imageDtos = mapImages(imagesToMap);
        List<PersonCapacityDTO> personCapacities = mapPersonCapacities(homestay.getListPersonHomestay());
        List<TouristAttractionDTO> touristAttractions = mapTouristAttractions(homestay.getListTourisAttractions());

        int reviewCount = homestay.getListReview() != null ? homestay.getListReview().size() : 0;

        return HomestayDTO.builder()
                .id(homestay.getId())
                .hostId(homestay.getHost() != null ? homestay.getHost().getId() : null)
                .title(homestay.getTitle())
                .description(homestay.getDescription())
                .category(homestay.getCategory())
                .rating(homestay.getRating())
                .numbersOfReview(reviewCount)
                .minGuest(homestay.getMinGuest())
                .maxGuest(homestay.getMaxGuest())
                .numBedrooms(homestay.getNumBedrooms())
                .numBeds(homestay.getNumBeds())
                .numBathrooms(homestay.getNumBathrooms())
                .numKitchen(homestay.getNumKitchen())
                .advancedPayment(homestay.getAdvancedPayment())
                .warningCount(homestay.getWarningCount())
                .basePrice(homestay.getBasePrice())
                .point(homestay.getPoint())
                .status(homestay.getStatusHomestay())
                .address(addressDTO)
                .facilities(facilities)
                .amenities(amenities)
                .rules(rules)
                .dailyPrices(dailyPrices)
                .images(imageDtos)
                .personCapacities(personCapacities)
                .touristAttractions(touristAttractions)
                .build();
    }

    public HomestaySummaryDTO toSummary(Homestay homestay) {
        if (homestay == null) {
            return null;
        }

        Address address = homestay.getAddress();
        return HomestaySummaryDTO.builder()
                .id(homestay.getId())
                .title(homestay.getTitle())
                .category(homestay.getCategory())
                .status(homestay.getStatusHomestay())
                .hostId(homestay.getHost() != null ? homestay.getHost().getId() : null)
                .hostName(homestay.getHost() != null && homestay.getHost().getUser() != null
                        ? homestay.getHost().getUser().getName()
                        : null)
                .city(address != null ? address.getCity() : null)
                .state(address != null ? address.getState() : null)
                .createdAt(homestay.getCreatedAt())
                .build();
    }

    private AddressDTO mapAddress(Address address) {
        if (address == null) {
            return null;
        }
        return AddressDTO.builder()
                .id(address.getId())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .url(address.getUrl())
                .build();
    }

    private List<HomestayFacilityDTO> mapFacilities(Set<Facilities> facilities) {
        if (facilities == null) {
            return List.of();
        }
        return facilities.stream()
                .filter(facility -> facility != null && !Boolean.TRUE.equals(facility.getDeleted()))
                .map(facility -> HomestayFacilityDTO.builder()
                        .id(facility.getId())
                        .name(facility.getName())
                        .category(facility.getCategory())
                        .build())
                .sorted(Comparator.comparing(HomestayFacilityDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<HomestayAmenityDTO> mapAmenities(Set<Amenities> amenities) {
        if (amenities == null) {
            return List.of();
        }
        return amenities.stream()
                .filter(amenity -> amenity != null && !Boolean.TRUE.equals(amenity.getDeleted()))
                .map(amenity -> HomestayAmenityDTO.builder()
                        .id(amenity.getId())
                        .name(amenity.getName())
                        .description(amenity.getDescription())
                        .imageUrl(amenity.getImageUrl())
                        .build())
                .sorted(Comparator.comparing(HomestayAmenityDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<HomestayRuleDTO> mapRules(Set<HomestayRule> rules) {
        if (rules == null) {
            return List.of();
        }
        return rules.stream()
                .map(rule -> HomestayRuleDTO.builder()
                        .id(rule.getId())
                        .description(rule.getDescription())
                        .ruleType(rule.getRuleTypeHomestay())
                        .build())
                .sorted(Comparator.comparing(HomestayRuleDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<HomestayDailyPriceDTO> mapDailyPrices(Set<HomestayDailyPrice> dailyPrices) {
        if (dailyPrices == null) {
            return List.of();
        }
        return dailyPrices.stream()
                .map(price -> HomestayDailyPriceDTO.builder()
                        .id(price.getId())
                        .day(price.getPricePerDay() != null ? price.getPricePerDay().getDay() : null)
                        .price(price.getPrice())
                        .booked(price.getIsBooked())
                        .activeHost(price.getActiveHost())
                        .build())
                .sorted(Comparator.comparing(HomestayDailyPriceDTO::getDay, Comparator.nullsLast(java.util.Date::compareTo)))
                .collect(Collectors.toList());
    }

    private List<HomestayImageDTO> mapImages(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .map(image -> HomestayImageDTO.builder()
                        .id(image.getId())
                        .imageUrl(image.getImage_url())
                        .primary(image.getIsPrimary())
                        .build())
                .sorted(Comparator.comparing(HomestayImageDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<PersonCapacityDTO> mapPersonCapacities(Set<PersonHomestay> capacities) {
        if (capacities == null) {
            return List.of();
        }
        return capacities.stream()
                .filter(capacity -> capacity != null && capacity.getPerson() != null)
                .map(capacity -> PersonCapacityDTO.builder()
                        .type(capacity.getPerson().getType())
                        .quantity(capacity.getQuantity())
                        .build())
                .filter(dto -> dto.getType() != null)
                .sorted(Comparator.comparing(dto -> dto.getType().ordinal()))
                .collect(Collectors.toList());
    }

    private List<TouristAttractionDTO> mapTouristAttractions(Set<TouristAttractions> touristAttractions) {
        if (touristAttractions == null) {
            return List.of();
        }
        return touristAttractions.stream()
                .filter(ta -> ta != null && !Boolean.TRUE.equals(ta.getDeleted()))
                .map(ta -> TouristAttractionDTO.builder()
                        .id(ta.getId())
                        .name(ta.getName())
                        .description(ta.getDescription())
                        .imageUrl(ta.getImageUrl())
                        .build())
                .sorted(Comparator.comparing(TouristAttractionDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }
}
