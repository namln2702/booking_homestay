package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.PreferenceDTO;
import org.example.do_an_v1.dto.request.PreferenceBatchCreateRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.PreferenceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    /**
     * Lấy tất cả Preferences
     * Public endpoint - không cần authentication
     */
    @GetMapping
    public ApiResponse<List<PreferenceDTO>> getAllPreferences() {
        return preferenceService.getAllPreferences();
    }

    /**
     * Lấy Preference theo ID
     * Public endpoint - không cần authentication
     */
    @GetMapping("/{id}")
    public ApiResponse<PreferenceDTO> getPreferenceById(@PathVariable Long id) {
        return preferenceService.getPreferenceById(id);
    }

    /**
     * Tạo mới Preference
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping
    public ApiResponse<PreferenceDTO> createPreference(@RequestBody @Valid PreferenceDTO preferenceDTO) {
        return preferenceService.createPreference(preferenceDTO);
    }

    /**
     * Tạo nhiều Preferences cùng lúc (batch create)
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping("/batch")
    public ApiResponse<List<PreferenceDTO>> createPreferencesBatch(@RequestBody @Valid PreferenceBatchCreateRequest request) {
        return preferenceService.createPreferencesBatch(request);
    }

    /**
     * Cập nhật Preference
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<PreferenceDTO> updatePreference(
            @PathVariable Long id,
            @RequestBody @Valid PreferenceDTO preferenceDTO) {
        return preferenceService.updatePreference(id, preferenceDTO);
    }

    /**
     * Xóa Preference
     * Yêu cầu quyền ADMIN hoặc SUPER_ADMIN
     */
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePreference(@PathVariable Long id) {
        return preferenceService.deletePreference(id);
    }
}

