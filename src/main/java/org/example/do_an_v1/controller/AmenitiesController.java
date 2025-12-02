package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AmenitiesDTO;
import org.example.do_an_v1.dto.request.AmenitiesBatchCreateRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.AmenitiesService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/amenities")
public class AmenitiesController {

    private final AmenitiesService amenitiesService;

    /**
     * Lấy tất cả Amenities
     * Public endpoint - không cần authentication
     */
    @GetMapping
    public ApiResponse<List<AmenitiesDTO>> getAllAmenities() {
        return amenitiesService.getAllAmenities();
    }

    /**
     * Lấy Amenities theo ID
     * Public endpoint - không cần authentication
     */
    @GetMapping("/{id}")
    public ApiResponse<AmenitiesDTO> getAmenityById(@PathVariable Long id) {
        return amenitiesService.getAmenityById(id);
    }

    /**
     * Tạo nhiều Amenities cùng lúc (batch create)
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping("/batch")
    public ApiResponse<List<AmenitiesDTO>> createAmenitiesBatch(@RequestBody @Valid AmenitiesBatchCreateRequest request) {
        return amenitiesService.createAmenitiesBatch(request);
    }

    /**
     * Cập nhật Amenity
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<AmenitiesDTO> updateAmenity(
            @PathVariable Long id,
            @RequestBody @Valid AmenitiesDTO amenitiesDTO) {
        return amenitiesService.updateAmenity(id, amenitiesDTO);
    }

    /**
     * Xóa Amenity
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteAmenity(@PathVariable Long id) {
        return amenitiesService.deleteAmenity(id);
    }
}

