package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.entity.Address;
import org.example.do_an_v1.entity.Amenities;
import org.example.do_an_v1.entity.Facilities;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.example.do_an_v1.entity.HomestayImage;
import org.example.do_an_v1.entity.HomestayRule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HomestayMapper {

    public HomestayDTO toDto(Homestay homestay, List<HomestayImage> images) {
        if (homestay == null) {
            return null;
        }

        HomestayDTO.AddressDTO addressDTO = mapAddress(homestay.getAddress());

        List<Long> facilityIds = mapFacilities(homestay.getListFacilities());
        List<HomestayDTO.AmenityDTO> amenities = mapAmenities(homestay.getListAmenities());
        List<HomestayDTO.HomestayRuleDTO> rules = mapRules(homestay.getListHomestayRule());
        List<HomestayDTO.DailyPriceDTO> dailyPrices = mapDailyPrices(homestay.getListHomestayDailyPrice());
        List<HomestayDTO.HomestayImageDTO> imageDtos = mapImages(images);

        return HomestayDTO.builder()
                .id(homestay.getId())
                .hostId(homestay.getHost() != null ? homestay.getHost().getId() : null)
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
                .status(homestay.getStatusHomestay())
                .address(addressDTO)
                .facilityIds(facilityIds)
                .amenities(amenities)
                .rules(rules)
                .dailyPrices(dailyPrices)
                .images(imageDtos)
                .build();
    }

    private HomestayDTO.AddressDTO mapAddress(Address address) {
        if (address == null) {
            return null;
        }
        return HomestayDTO.AddressDTO.builder()
                .addressLine(address.getAddress_line())
                .city(address.getCity())
                .state(address.getState())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    private List<Long> mapFacilities(Set<Facilities> facilities) {
        if (facilities == null) {
            return List.of();
        }
        return facilities.stream()
                .map(Facilities::getId)
                .sorted()
                .collect(Collectors.toList());
    }

    private List<HomestayDTO.AmenityDTO> mapAmenities(Set<Amenities> amenities) {
        if (amenities == null) {
            return List.of();
        }
        return amenities.stream()
                .map(amenity -> HomestayDTO.AmenityDTO.builder()
                        .id(amenity.getId())
                        .name(amenity.getName())
                        .description(amenity.getDescription())
                        .imageUrl(amenity.getImageUrl())
                        .build())
                .sorted(Comparator.comparing(HomestayDTO.AmenityDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<HomestayDTO.HomestayRuleDTO> mapRules(Set<HomestayRule> rules) {
        if (rules == null) {
            return List.of();
        }
        return rules.stream()
                .map(rule -> HomestayDTO.HomestayRuleDTO.builder()
                        .id(rule.getId())
                        .description(rule.getDescription())
                        .ruleType(rule.getRuleTypeHomestay())
                        .build())
                .sorted(Comparator.comparing(HomestayDTO.HomestayRuleDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<HomestayDTO.DailyPriceDTO> mapDailyPrices(Set<HomestayDailyPrice> dailyPrices) {
        if (dailyPrices == null) {
            return List.of();
        }
        return dailyPrices.stream()
                .map(price -> HomestayDTO.DailyPriceDTO.builder()
                        .id(price.getId())
                        .day(price.getPricePerDay() != null ? price.getPricePerDay().getDay() : null)
                        .price(price.getPrice())
                        .booked(price.getIsBooked())
                        .build())
                .sorted(Comparator.comparing(HomestayDTO.DailyPriceDTO::getDay, Comparator.nullsLast(java.util.Date::compareTo)))
                .collect(Collectors.toList());
    }

    private List<HomestayDTO.HomestayImageDTO> mapImages(List<HomestayImage> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(image -> HomestayDTO.HomestayImageDTO.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .primary(image.getIsPrimary())
                        .build())
                .sorted(Comparator.comparing(HomestayDTO.HomestayImageDTO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }
}
