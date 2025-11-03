package org.example.do_an_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.do_an_v1.enums.RuleTypeHomestay;
import org.example.do_an_v1.enums.StatusHomestay;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayDTO {

    private Long id;
    private Long hostId;
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
    private StatusHomestay status;

    private AddressDTO address;
    private List<Long> facilityIds;
    private List<AmenityDTO> amenities;
    private List<HomestayRuleDTO> rules;
    private List<DailyPriceDTO> dailyPrices;
    private List<HomestayImageDTO> images;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private String addressLine;
        private String city;
        private String state;
        private String latitude;
        private String longitude;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AmenityDTO {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HomestayRuleDTO {
        private Long id;
        private String description;
        private RuleTypeHomestay ruleType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyPriceDTO {
        private Long id;
        private Date day;
        private Float price;
        private Boolean booked;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HomestayImageDTO {
        private Long id;
        private String imageUrl;
        private Boolean primary;
    }
}
