package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Amenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenitiesRepository extends JpaRepository<Amenities, Long> {
    
    /**
     * Tìm tất cả amenities chưa bị xóa
     */
    List<Amenities> findByDeletedFalse();
    
    /**
     * Tìm amenity theo ID và chưa bị xóa
     */
    java.util.Optional<Amenities> findByIdAndDeletedFalse(Long id);
}
