package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.*;
import org.example.do_an_v1.entity.*;
import java.util.stream.Collectors;

public class HomestayMapper {

    // -----------------------------
    // Homestay → HomestayDVO
    // -----------------------------
    public static HomestayDTO toDTO(Homestay h) {
        if (h == null) return null;

        return HomestayDTO.builder()
                .id(h.getId())
                .title(h.getTitle())
                .description(h.getDescription())
                .category(h.getCategory())
                .rating(h.getRating())
                .maxGuest(h.getMaxGuest())
                .minGuest(h.getMinGuest())
                .numBedrooms(h.getNumBedrooms())
                .numBeds(h.getNumBeds())
                .numBathrooms(h.getNumBathrooms())
                .numKitchen(h.getNumKitchen())
                .statusHomestay(h.getStatusHomestay())
                .advancedPayment(h.getAdvancedPayment())
                .warningCount(h.getWarningCount())

                .hostId(h.getHost() != null ? h.getHost().getId() : null)
                .hostName(h.getHost() != null ? h.getHost().getBusinessName() : null)

                .addressId(h.getAddress() != null ? h.getAddress().getId() : null)
                .addressLine(h.getAddress() != null ? h.getAddress().getAddressLine() : null)
                .latitude(h.getAddress() != null ? h.getAddress().getLatitude() : null)
                .longitude(h.getAddress() != null ? h.getAddress().getLongitude() : null)

                .imageUrls(h.getListImage() != null
                        ? h.getListImage().stream().map(ImageMapper::toDTO).collect(Collectors.toList())
                        : null)

                .facilities(h.getListFacilities() != null
                        ? h.getListFacilities().stream().map(FacilitiesMapper::toDTO).collect(Collectors.toList())
                        : null)

                .amenities(h.getListAmenities() != null
                        ? h.getListAmenities().stream().map(AmenitiesMapper::toDTO).collect(Collectors.toList())
                        : null)

                .homestayRules(h.getListHomestayRule() != null
                        ? h.getListHomestayRule().stream().map(HomestayRulesMapper::toDTO).collect(Collectors.toList())
                        : null)

                .dailyPrices(h.getListHomestayDailyPrice() != null
                        ? h.getListHomestayDailyPrice().stream().map(DailyPriceMapper::toDVO).collect(Collectors.toList())
                        : null)

                .build();
    }

    // -----------------------------
    // HomestayDVO → Homestay
    // -----------------------------
    public static Homestay toEntity(HomestayDTO dto) {
        if (dto == null) return null;

        Homestay homestay = new Homestay();
        homestay.setId(dto.getId());
        homestay.setTitle(dto.getTitle());
        homestay.setDescription(dto.getDescription());
        homestay.setCategory(dto.getCategory());
        homestay.setRating(dto.getRating());
        homestay.setMaxGuest(dto.getMaxGuest());
        homestay.setMinGuest(dto.getMinGuest());
        homestay.setNumBedrooms(dto.getNumBedrooms());
        homestay.setNumBeds(dto.getNumBeds());
        homestay.setNumBathrooms(dto.getNumBathrooms());
        homestay.setNumKitchen(dto.getNumKitchen());
        homestay.setStatusHomestay(dto.getStatusHomestay());
        homestay.setAdvancedPayment(dto.getAdvancedPayment());
        homestay.setWarningCount(dto.getWarningCount());

        // Host
        if (dto.getHostId() != null) {
            Host host = new Host();
            host.setId(dto.getHostId());
            host.setBusinessName(dto.getHostName());
            homestay.setHost(host);
        }

        // Address
        if (dto.getAddressId() != null) {
            Address address = new Address();
            address.setId(dto.getAddressId());
            address.setAddressLine(dto.getAddressLine());
            address.setLatitude(dto.getLatitude());
            address.setLongitude(dto.getLongitude());
            homestay.setAddress(address);
        }

        // Image list
        if (dto.getImageUrls() != null) {
            homestay.setListImage(dto.getImageUrls().stream()
                    .map(ImageMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        // Facilities list
        if (dto.getFacilities() != null) {
            homestay.setListFacilities(dto.getFacilities().stream()
                    .map(FacilitiesMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        // Amenities list
        if (dto.getAmenities() != null) {
            homestay.setListAmenities(dto.getAmenities().stream()
                    .map(AmenitiesMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        // Rules list
        if (dto.getHomestayRules() != null) {
            homestay.setListHomestayRule(dto.getHomestayRules().stream()
                    .map(HomestayRulesMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        // Daily Prices list
        if (dto.getDailyPrices() != null) {
            homestay.setListHomestayDailyPrice(dto.getDailyPrices().stream()
                    .map(DailyPriceMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        return homestay;
    }
}
