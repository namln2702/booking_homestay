package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.TouristAttractions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristAttractionsRepository extends JpaRepository<TouristAttractions, Long> {
    List<TouristAttractions> findByHomestayIdAndDeletedFalse(Long homestayId);
}

