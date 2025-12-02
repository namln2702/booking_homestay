package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.PreferenceDTO;
import org.example.do_an_v1.dto.request.PreferenceBatchCreateRequest;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface PreferenceService {

    /**
     * Lấy tất cả Preferences
     * @return ApiResponse chứa danh sách PreferenceDTO
     */
    ApiResponse<List<PreferenceDTO>> getAllPreferences();

    /**
     * Lấy Preference theo ID
     * @param id ID của Preference
     * @return ApiResponse chứa PreferenceDTO
     */
    ApiResponse<PreferenceDTO> getPreferenceById(Long id);

    /**
     * Tạo mới Preference
     * @param preferenceDTO DTO chứa thông tin Preference cần tạo
     * @return ApiResponse chứa PreferenceDTO đã tạo
     */
    ApiResponse<PreferenceDTO> createPreference(PreferenceDTO preferenceDTO);

    /**
     * Tạo nhiều Preferences cùng lúc (batch create)
     * @param request Danh sách Preferences cần tạo
     * @return ApiResponse chứa danh sách PreferenceDTO đã tạo
     */
    ApiResponse<List<PreferenceDTO>> createPreferencesBatch(PreferenceBatchCreateRequest request);

    /**
     * Cập nhật Preference
     * @param id ID của Preference cần cập nhật
     * @param preferenceDTO DTO chứa thông tin cập nhật
     * @return ApiResponse chứa PreferenceDTO đã cập nhật
     */
    ApiResponse<PreferenceDTO> updatePreference(Long id, PreferenceDTO preferenceDTO);

    /**
     * Xóa Preference
     * @param id ID của Preference cần xóa
     * @return ApiResponse thông báo kết quả
     */
    ApiResponse<?> deletePreference(Long id);
}

