package org.example.do_an_v1.service;

import org.example.do_an_v1.entity.SystemConfig;

import java.math.BigDecimal;
import java.util.Optional;

public interface SystemConfigService {
    
    /**
     * Lấy config value theo key
     */
    Optional<SystemConfig> getConfigByKey(String key);
    
    /**
     * Lấy config value dạng String
     */
    Optional<String> getConfigValue(String key);
    
    /**
     * Lấy config value dạng BigDecimal (dùng cho commission)
     */
    Optional<BigDecimal> getConfigValueAsBigDecimal(String key);
    
    /**
     * Tạo mới hoặc cập nhật SystemConfig
     * Nếu key đã tồn tại thì cập nhật value và description
     * Nếu key chưa tồn tại thì tạo mới
     */
    SystemConfig createOrUpdateConfig(String key, String value, String description);
}

