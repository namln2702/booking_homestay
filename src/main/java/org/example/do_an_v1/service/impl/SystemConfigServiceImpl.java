package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.SystemConfig;
import org.example.do_an_v1.repository.SystemConfigRepository;
import org.example.do_an_v1.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<SystemConfig> getConfigByKey(String key) {
        return systemConfigRepository.findByKey(key);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getConfigValue(String key) {
        return systemConfigRepository.findByKey(key)
                .map(SystemConfig::getValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getConfigValueAsBigDecimal(String key) {
        return systemConfigRepository.findByKey(key)
                .map(SystemConfig::getValue)
                .map(value -> {
                    try {
                        return new BigDecimal(value);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse config value '{}' as BigDecimal for key '{}': {}", value, key, e.getMessage());
                        return null;
                    }
                });
    }

    @Override
    @Transactional
    public SystemConfig createOrUpdateConfig(String key, String value, String description) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Config key cannot be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Config value cannot be null or empty");
        }

        Optional<SystemConfig> existingConfig = systemConfigRepository.findByKey(key);
        
        if (existingConfig.isPresent()) {
            // Cập nhật config đã tồn tại
            SystemConfig config = existingConfig.get();
            config.setValue(value);
            if (description != null) {
                config.setDescription(description);
            }
            SystemConfig saved = systemConfigRepository.save(config);
            log.info("Updated system config with key: {}", key);
            return saved;
        } else {
            // Tạo mới config
            SystemConfig newConfig = SystemConfig.builder()
                    .key(key)
                    .value(value)
                    .description(description != null ? description : "")
                    .build();
            SystemConfig saved = systemConfigRepository.save(newConfig);
            log.info("Created new system config with key: {}", key);
            return saved;
        }
    }
}

