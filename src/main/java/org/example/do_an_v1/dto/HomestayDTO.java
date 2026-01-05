package org.example.do_an_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.do_an_v1.enums.StatusHomestay;

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
    private Integer numbersOfReview;
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

    private AddressDTO address;
    private List<HomestayFacilityDTO> facilities;
    private List<HomestayAmenityDTO> amenities;
    private List<HomestayRuleDTO> rules;
    private List<HomestayDailyPriceDTO> dailyPrices;
    private List<HomestayImageDTO> images;
    private List<PersonCapacityDTO> personCapacities;
    private List<TouristAttractionDTO> touristAttractions;
}
