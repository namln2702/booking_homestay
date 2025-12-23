package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Preference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    
    /**
     * Tìm tất cả preferences chưa bị xóa
     */
    List<Preference> findByDeletedFalse();
    
    /**
     * Tìm preference theo ID và chưa bị xóa
     */
    java.util.Optional<Preference> findByIdAndDeletedFalse(Long id);
}
