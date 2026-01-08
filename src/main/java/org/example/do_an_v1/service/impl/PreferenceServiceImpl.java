package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.PreferenceDTO;
import org.example.do_an_v1.dto.request.PreferenceBatchCreateRequest;
import org.example.do_an_v1.entity.Preference;
import org.example.do_an_v1.exception.ResourceNotFoundException;
import org.example.do_an_v1.mapper.PreferenceMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.PreferenceRepository;
import org.example.do_an_v1.service.PreferenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceRepository preferenceRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PreferenceDTO>> getAllPreferences() {
        List<Preference> preferences = preferenceRepository.findByDeletedFalse();

        List<PreferenceDTO> preferenceDTOS = preferences.stream()
                .map(PreferenceMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Preferences retrieved successfully", preferenceDTOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PreferenceDTO> getPreferenceById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Preference id is required");
        }

        Preference preference = preferenceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found with id: " + id));

        PreferenceDTO preferenceDTO = PreferenceMapper.toDTO(preference);

        return new ApiResponse<>(200, "Preference retrieved successfully", preferenceDTO);
    }

    @Override
    @Transactional
    public ApiResponse<PreferenceDTO> createPreference(PreferenceDTO preferenceDTO) {
        if (preferenceDTO == null) {
            throw new IllegalArgumentException("Preference DTO is required");
        }
        if (preferenceDTO.getName() == null || preferenceDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Preference name is required");
        }

        // Kiểm tra xem đã tồn tại preference với tên này chưa (chỉ check những cái chưa bị xóa)
        List<Preference> existingPreferences = preferenceRepository.findByDeletedFalse();
        boolean nameExists = existingPreferences.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(preferenceDTO.getName().trim()));
        
        if (nameExists) {
            throw new IllegalArgumentException("Preference with name '" + preferenceDTO.getName() + "' already exists");
        }

        Preference preference = Preference.builder()
                .name(preferenceDTO.getName().trim())
                .description(preferenceDTO.getDescription() != null ? preferenceDTO.getDescription().trim() : null)
                .deleted(false)
                .build();

        Preference savedPreference = preferenceRepository.save(preference);
        PreferenceDTO responseDTO = PreferenceMapper.toDTO(savedPreference);

        return new ApiResponse<>(201, "Preference created successfully", responseDTO);
    }

    @Override
    @Transactional
    public ApiResponse<PreferenceDTO> updatePreference(Long id, PreferenceDTO preferenceDTO) {
        if (id == null) {
            throw new IllegalArgumentException("Preference id is required");
        }
        if (preferenceDTO == null) {
            throw new IllegalArgumentException("Preference DTO is required");
        }

        Preference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found with id: " + id));

        // Cập nhật name nếu có
        if (preferenceDTO.getName() != null && !preferenceDTO.getName().trim().isEmpty()) {
            // Kiểm tra xem tên mới có trùng với preference khác không (chỉ check những cái chưa bị xóa)
            List<Preference> existingPreferences = preferenceRepository.findByDeletedFalse();
            boolean nameExists = existingPreferences.stream()
                    .anyMatch(p -> !p.getId().equals(id) && p.getName().equalsIgnoreCase(preferenceDTO.getName().trim()));
            
            if (nameExists) {
                throw new IllegalArgumentException("Preference with name '" + preferenceDTO.getName() + "' already exists");
            }
            
            preference.setName(preferenceDTO.getName().trim());
        }

        // Cập nhật description nếu có
        if (preferenceDTO.getDescription() != null) {
            preference.setDescription(preferenceDTO.getDescription().trim());
        }

        Preference savedPreference = preferenceRepository.save(preference);
        PreferenceDTO responseDTO = PreferenceMapper.toDTO(savedPreference);

        return new ApiResponse<>(200, "Preference updated successfully", responseDTO);
    }

    @Override
    @Transactional
    public ApiResponse<?> deletePreference(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Preference id is required");
        }

        Preference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found with id: " + id));

        // Kiểm tra xem preference có đang được sử dụng bởi customer nào không
//        if (preference.getListCustomer() != null && !preference.getListCustomer().isEmpty()) {
//            throw new IllegalStateException("Cannot delete preference. It is currently associated with " +
//                    preference.getListCustomer().size() + " customer(s)");
//        }

        // Soft delete: set deleted = true
        preference.setDeleted(true);
        preferenceRepository.save(preference);

        return new ApiResponse<>(200, "Preference deleted successfully", null);
    }

    @Override
    @Transactional
    public ApiResponse<List<PreferenceDTO>> createPreferencesBatch(PreferenceBatchCreateRequest request) {
        if (request == null || request.getPreferences() == null || request.getPreferences().isEmpty()) {
            throw new IllegalArgumentException("Preferences list cannot be null or empty");
        }

        if (request.getPreferences().size() > 50) {
            throw new IllegalArgumentException("Cannot create more than 50 preferences at once");
        }

        // Lấy danh sách preferences hiện có để kiểm tra trùng tên (chỉ check những cái chưa bị xóa)
        List<Preference> existingPreferences = preferenceRepository.findByDeletedFalse();
        Set<String> existingNames = existingPreferences.stream()
                .map(p -> p.getName().toLowerCase())
                .collect(Collectors.toSet());

        // Validate từng preference trong list và kiểm tra trùng tên trong request
        Set<String> requestNames = new HashSet<>();
        for (int i = 0; i < request.getPreferences().size(); i++) {
            PreferenceDTO preferenceDTO = request.getPreferences().get(i);
            if (preferenceDTO == null) {
                throw new IllegalArgumentException("Preference at index " + i + " is null");
            }
            if (preferenceDTO.getName() == null || preferenceDTO.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Preference name is required at index " + i);
            }

            String normalizedName = preferenceDTO.getName().trim().toLowerCase();
            
            // Kiểm tra trùng tên trong request
            if (requestNames.contains(normalizedName)) {
                throw new IllegalArgumentException("Duplicate preference name '" + preferenceDTO.getName() + "' in request at index " + i);
            }
            requestNames.add(normalizedName);

            // Kiểm tra trùng tên với preferences đã tồn tại
            if (existingNames.contains(normalizedName)) {
                throw new IllegalArgumentException("Preference with name '" + preferenceDTO.getName() + "' already exists (at index " + i + ")");
            }
        }

        // Tạo danh sách Preferences entities
        List<Preference> preferencesToSave = request.getPreferences().stream()
                .map(preferenceDTO -> Preference.builder()
                        .name(preferenceDTO.getName().trim())
                        .description(preferenceDTO.getDescription() != null ? preferenceDTO.getDescription().trim() : null)
                        .deleted(false)
                        .build())
                .collect(Collectors.toList());

        // Lưu tất cả vào database
        List<Preference> savedPreferences = preferenceRepository.saveAll(preferencesToSave);

        // Map sang DTO
        List<PreferenceDTO> preferenceDTOS = savedPreferences.stream()
                .map(PreferenceMapper::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(201, "Preferences created successfully", preferenceDTOS);
    }
}

