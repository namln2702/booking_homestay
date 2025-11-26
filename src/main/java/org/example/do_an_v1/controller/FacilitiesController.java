package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.FacilitiesDTO;
import org.example.do_an_v1.dto.request.FacilitiesBatchCreateRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.FacilitiesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/facilities")
public class FacilitiesController {

    private final FacilitiesService facilitiesService;

    /**
     * Lấy tất cả Facilities
     * Public endpoint - không cần authentication
     */
    @GetMapping
    public ApiResponse<List<FacilitiesDTO>> getAllFacilities() {
        return facilitiesService.getAllFacilities();
    }

    /**
     * Lấy Facilities theo ID
     * Public endpoint - không cần authentication
     */
    @GetMapping("/{id}")
    public ApiResponse<FacilitiesDTO> getFacilityById(@PathVariable Long id) {
        return facilitiesService.getFacilityById(id);
    }

    /**
     * Tạo nhiều Facilities cùng lúc (batch create)
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping("/batch")
    public ApiResponse<List<FacilitiesDTO>> createFacilitiesBatch(@RequestBody @Valid FacilitiesBatchCreateRequest request) {
        return facilitiesService.createFacilitiesBatch(request);
    }
}

