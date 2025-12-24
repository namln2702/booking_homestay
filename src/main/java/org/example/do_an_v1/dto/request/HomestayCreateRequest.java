package org.example.do_an_v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayCreateRequest {
    private String title;
    private String description;
    private String category;

    private Integer minGuest;
    private Integer maxGuest;
    private Integer numBedrooms;
    private Integer numBeds;
    private Integer numBathrooms;
    private Integer numKitchen;
    private Float basePrice;

    private AddressRequest address;
    private List<Long> facilitiesIds;         // chọn từ hệ thống
    private List<AmenityRequest> amenities;   // host tự đăng
    private List<HomestayImageRequest> imageUrls;
    private List<HomestayRuleRequest> rules;
    private List<HomestayDailyPriceRequest> dailyPrices;
    @Valid
    @NotEmpty(message = "listPersonHomestay is required")
    private List<PersonCapacityRequest> listPersonHomestay;
}
