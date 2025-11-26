package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.FacilitiesDTO;
import org.example.do_an_v1.dto.request.FacilitiesBatchCreateRequest;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface FacilitiesService {

    /**
     * Lấy tất cả Facilities
     * @return ApiResponse chứa danh sách FacilitiesDTO
     */
    ApiResponse<List<FacilitiesDTO>> getAllFacilities();

    /**
     * Lấy Facilities theo ID
     * @param id ID của Facilities
     * @return ApiResponse chứa FacilitiesDTO
     */
    ApiResponse<FacilitiesDTO> getFacilityById(Long id);

    /**
     * Tạo nhiều Facilities cùng lúc (batch create)
     * @param request Danh sách Facilities cần tạo
     * @return ApiResponse chứa danh sách FacilitiesDTO đã tạo
     */
    ApiResponse<List<FacilitiesDTO>> createFacilitiesBatch(FacilitiesBatchCreateRequest request);
}

