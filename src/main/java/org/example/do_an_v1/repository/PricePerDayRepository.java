package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.PricePerDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PricePerDayRepository extends JpaRepository<PricePerDay, Long> {

    Optional<PricePerDay> findByDay(Date day);
}
