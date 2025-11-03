package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Facilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FacilitiesRepository extends JpaRepository<Facilities, Long> {

    List<Facilities> findByIdIn(Collection<Long> ids);
}
