package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.AmenitiesDTO;
import org.example.do_an_v1.dto.request.AmenitiesBatchCreateRequest;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface AmenitiesService {

    /**
     * Lấy tất cả Amenities
     * @return ApiResponse chứa danh sách AmenitiesDTO
     */
    ApiResponse<List<AmenitiesDTO>> getAllAmenities();

    /**
     * Lấy Amenities theo ID
     * @param id ID của Amenities
     * @return ApiResponse chứa AmenitiesDTO
     */
    ApiResponse<AmenitiesDTO> getAmenityById(Long id);

    /**
     * Tạo nhiều Amenities cùng lúc (batch create)
     * @param request Danh sách Amenities cần tạo
     * @return ApiResponse chứa danh sách AmenitiesDTO đã tạo
     */
    ApiResponse<List<AmenitiesDTO>> createAmenitiesBatch(AmenitiesBatchCreateRequest request);

    /**
     * Cập nhật Amenity
     * @param id ID của Amenity cần cập nhật
     * @param amenitiesDTO DTO chứa thông tin cập nhật
     * @return ApiResponse chứa AmenitiesDTO đã cập nhật
     */
    ApiResponse<AmenitiesDTO> updateAmenity(Long id, AmenitiesDTO amenitiesDTO);

    /**
     * Xóa Amenity
     * @param id ID của Amenity cần xóa
     * @return ApiResponse thông báo kết quả
     */
    ApiResponse<?> deleteAmenity(Long id);
}

