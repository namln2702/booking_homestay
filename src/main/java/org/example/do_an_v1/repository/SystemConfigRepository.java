package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    /**
     * Tìm config theo key
     */
    Optional<SystemConfig> findByKey(String key);
}

