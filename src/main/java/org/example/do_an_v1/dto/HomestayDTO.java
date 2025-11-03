package org.example.do_an_v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.do_an_v1.enums.StatusHomestay;

import java.util.List;

@Builder
@Getter
@Setter
public class HomestayDTO {
    private Long id;                     // ID homestay (từ BaseEntity)
    private String title;                // Tiêu đề
    private String description;          // Mô tả
    private String category;             // Danh mục
    private Float rating;                // Điểm đánh giá trung bình

    private Integer maxGuest;            // Số khách tối đa
    private Integer minGuest;            // Số khách tối thiểu
    private Integer numBedrooms;         // Số phòng ngủ
    private Integer numBeds;             // Số giường
    private Integer numBathrooms;        // Số phòng tắm
    private Integer numKitchen;          // Số bếp

    private StatusHomestay statusHomestay; // Trạng thái (ACTIVE, BANNED, ...)

    private Integer advancedPayment;     // Tiền đặt cọc
    private Integer warningCount;        // Số lần cảnh cáo

    // Quan hệ
    private Long hostId;                 // ID chủ homestay
    private String hostName;             // Tên chủ homestay

    private Long addressId;              // ID địa chỉ
    private String addressLine;        // Địa chỉ chi tiết
    private String latitude;
    private String longitude;

    private List<ImageDTO> imageUrls;      // Danh sách URL ảnh
    private List<FacilitiesDTO> facilities;     // Tên các tiện nghi (Facilities)
    private List<AmenitiesDTO> amenities;      // Tên các tiện ích (Amenities)
    private List<HomestayRulesDTO> homestayRules;  // Danh sách quy tắc
    private List<DailyPriceDVO> dailyPrices;    // Giá theo ngày (string hoặc DTO riêng)

}
