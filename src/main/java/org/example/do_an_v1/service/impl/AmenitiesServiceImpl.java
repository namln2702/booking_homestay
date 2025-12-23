package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AmenitiesDTO;
import org.example.do_an_v1.dto.request.AmenitiesBatchCreateRequest;
import org.example.do_an_v1.entity.Amenities;
import org.example.do_an_v1.exception.ResourceNotFoundException;
import org.example.do_an_v1.mapper.AmenitiesMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.AmenitiesRepository;
import org.example.do_an_v1.service.AmenitiesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmenitiesServiceImpl implements AmenitiesService {

    private final AmenitiesRepository amenitiesRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AmenitiesDTO>> getAllAmenities() {
        List<Amenities> amenities = amenitiesRepository.findByDeletedFalse();

        List<AmenitiesDTO> amenitiesDTOS = amenities.stream()
                .map(AmenitiesMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Amenities retrieved successfully", amenitiesDTOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AmenitiesDTO> getAmenityById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Amenity id is required");
        }

        Amenities amenity = amenitiesRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Amenity not found for id " + id));

        AmenitiesDTO amenityDTO = AmenitiesMapper.toDTO(amenity);

        return new ApiResponse<>(200, "Amenity retrieved successfully", amenityDTO);
    }

    @Override
    @Transactional
    public ApiResponse<List<AmenitiesDTO>> createAmenitiesBatch(AmenitiesBatchCreateRequest request) {
        if (request == null || request.getAmenities() == null || request.getAmenities().isEmpty()) {
            throw new IllegalArgumentException("Amenities list cannot be null or empty");
        }

        if (request.getAmenities().size() > 50) {
            throw new IllegalArgumentException("Cannot create more than 50 amenities at once");
        }

        // Validate từng amenity trong list
        for (int i = 0; i < request.getAmenities().size(); i++) {
            AmenitiesDTO amenityDTO = request.getAmenities().get(i);
            if (amenityDTO == null) {
                throw new IllegalArgumentException("Amenity at index " + i + " is null");
            }
            if (amenityDTO.getName() == null || amenityDTO.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Amenity name is required at index " + i);
            }
            if (amenityDTO.getDescription() == null || amenityDTO.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Amenity description is required at index " + i);
            }
            if (amenityDTO.getImageUrl() == null || amenityDTO.getImageUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Amenity image URL is required at index " + i);
            }
        }

        // Tạo danh sách Amenities entities
        List<Amenities> amenitiesToSave = request.getAmenities().stream()
                .map(amenityDTO -> Amenities.builder()
                        .name(amenityDTO.getName().trim())
                        .description(amenityDTO.getDescription().trim())
                        .imageUrl(amenityDTO.getImageUrl().trim())
                        .deleted(false)
                        .build())
                .collect(Collectors.toList());

        // Lưu tất cả vào database
        List<Amenities> savedAmenities = amenitiesRepository.saveAll(amenitiesToSave);

        // Map sang DTO
        List<AmenitiesDTO> amenitiesDTOS = savedAmenities.stream()
                .map(AmenitiesMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(201, "Amenities created successfully", amenitiesDTOS);
    }

    @Override
    @Transactional
    public ApiResponse<AmenitiesDTO> updateAmenity(Long id, AmenitiesDTO amenitiesDTO) {
        if (id == null) {
            throw new IllegalArgumentException("Amenity id is required");
        }
        if (amenitiesDTO == null) {
            throw new IllegalArgumentException("Amenity DTO is required");
        }

        Amenities amenity = amenitiesRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        // Cập nhật name nếu có
        if (amenitiesDTO.getName() != null && !amenitiesDTO.getName().trim().isEmpty()) {
            amenity.setName(amenitiesDTO.getName().trim());
        }

        // Cập nhật description nếu có
        if (amenitiesDTO.getDescription() != null && !amenitiesDTO.getDescription().trim().isEmpty()) {
            amenity.setDescription(amenitiesDTO.getDescription().trim());
        }

        // Cập nhật imageUrl nếu có
        if (amenitiesDTO.getImageUrl() != null && !amenitiesDTO.getImageUrl().trim().isEmpty()) {
            amenity.setImageUrl(amenitiesDTO.getImageUrl().trim());
        }

        Amenities savedAmenity = amenitiesRepository.save(amenity);
        AmenitiesDTO responseDTO = AmenitiesMapper.toDTO(savedAmenity);

        return new ApiResponse<>(200, "Amenity updated successfully", responseDTO);
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteAmenity(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Amenity id is required");
        }

        Amenities amenity = amenitiesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        // Kiểm tra xem amenity có đang được sử dụng bởi homestay nào không
        if (amenity.getListHomestay() != null && !amenity.getListHomestay().isEmpty()) {
            throw new IllegalStateException("Cannot delete amenity. It is currently associated with " + 
                    amenity.getListHomestay().size() + " homestay(s)");
        }

        // Soft delete: set deleted = true
        amenity.setDeleted(true);
        amenitiesRepository.save(amenity);

        return new ApiResponse<>(200, "Amenity deleted successfully", null);
    }
}

