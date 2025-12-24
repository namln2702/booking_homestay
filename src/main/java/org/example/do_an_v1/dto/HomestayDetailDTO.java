package org.example.do_an_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.do_an_v1.dto.HomestayDTO.AmenityDTO;
import org.example.do_an_v1.dto.HomestayDTO.DailyPriceDTO;
import org.example.do_an_v1.dto.HomestayDTO.FacilityDTO;
import org.example.do_an_v1.dto.HomestayDTO.HomestayImageDTO;
import org.example.do_an_v1.dto.HomestayDTO.HomestayRuleDTO;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.enums.StatusHost;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated view of a homestay with all related data needed by clients:
 * base homestay info (address, facilities, host, rules, images, daily prices),
 * customer reviews, and pricing insights for the upcoming booking window.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayDetailDTO {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Float rating;
    private Integer minGuest;
    private Integer maxGuest;
    private Integer numBedrooms;
    private Integer numBeds;
    private Integer numBathrooms;
    private Integer numKitchen;
    private Integer advancedPayment;
    private Integer warningCount;
    private Float basePrice;
    private StatusHomestay status;

    private HomestayDTO.AddressDTO address;
    private HostSummaryDTO host;
    private List<FacilityDTO> facilities;
    private List<AmenityDTO> amenities;
    private List<HomestayRuleDTO> rules;
    private List<DailyPriceDTO> dailyPrices;
    private List<HomestayImageDTO> images;
    private List<HomestayDTO.PersonCapacityDTO> personCapacities;
    private PriceInsightDTO priceInsight;
    private List<ReviewDetailDTO> reviews;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HostSummaryDTO {
        private Long hostId;
        private StatusHost status;
        private String businessName;
        private String qrCodeUrl;
        private Long userId;
        private String fullName;
        private String avatarUrl;
        private String phone;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceInsightDTO {
        private Float minPrice;
        private Float maxPrice;
        private Float averagePrice;
        private Integer totalNights;
        private Integer availableNights;
        private Integer bookedNights;
        private LocalDate firstDate;
        private LocalDate lastDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDetailDTO {
        private Long id;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
        private ReviewerDTO reviewer;
        private List<ImageDTO> images;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewerDTO {
        private Long id;
        private String name;
        private String avatarUrl;
        private String email;
        private String phone;
        private String lastBooking;
    }
}
