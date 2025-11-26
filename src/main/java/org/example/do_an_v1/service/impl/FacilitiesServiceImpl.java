package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.FacilitiesDTO;
import org.example.do_an_v1.dto.request.FacilitiesBatchCreateRequest;
import org.example.do_an_v1.dto.request.FacilitiesCreateRequest;
import org.example.do_an_v1.entity.Facilities;
import org.example.do_an_v1.mapper.FacilitiesMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.FacilitiesRepository;
import org.example.do_an_v1.service.FacilitiesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilitiesServiceImpl implements FacilitiesService {

    private final FacilitiesRepository facilitiesRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<FacilitiesDTO>> getAllFacilities() {
        List<Facilities> facilities = facilitiesRepository.findAll();

        List<FacilitiesDTO> facilitiesDTOS = facilities.stream()
                .map(FacilitiesMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Facilities retrieved successfully", facilitiesDTOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<FacilitiesDTO> getFacilityById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Facility id is required");
        }

        Facilities facility = facilitiesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found for id " + id));

        FacilitiesDTO facilityDTO = FacilitiesMapper.toDTO(facility);

        return new ApiResponse<>(200, "Facility retrieved successfully", facilityDTO);
    }

    @Override
    @Transactional
    public ApiResponse<List<FacilitiesDTO>> createFacilitiesBatch(FacilitiesBatchCreateRequest request) {
        if (request == null || request.getFacilities() == null || request.getFacilities().isEmpty()) {
            throw new IllegalArgumentException("Facilities list cannot be null or empty");
        }

        if (request.getFacilities().size() > 50) {
            throw new IllegalArgumentException("Cannot create more than 50 facilities at once");
        }

        // Validate từng facility trong list
        for (int i = 0; i < request.getFacilities().size(); i++) {
            FacilitiesCreateRequest facilityRequest = request.getFacilities().get(i);
            if (facilityRequest == null) {
                throw new IllegalArgumentException("Facility at index " + i + " is null");
            }
            if (facilityRequest.getName() == null || facilityRequest.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Facility name is required at index " + i);
            }
            if (facilityRequest.getCategory() == null || facilityRequest.getCategory().trim().isEmpty()) {
                throw new IllegalArgumentException("Facility category is required at index " + i);
            }
        }

        // Tạo danh sách Facilities entities
        List<Facilities> facilitiesToSave = request.getFacilities().stream()
                .map(facilityRequest -> Facilities.builder()
                        .name(facilityRequest.getName().trim())
                        .category(facilityRequest.getCategory().trim())
                        .build())
                .collect(Collectors.toList());

        // Lưu tất cả vào database
        List<Facilities> savedFacilities = facilitiesRepository.saveAll(facilitiesToSave);

        // Map sang DTO
        List<FacilitiesDTO> facilitiesDTOS = savedFacilities.stream()
                .map(FacilitiesMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(201, "Facilities created successfully", facilitiesDTOS);
    }
}

