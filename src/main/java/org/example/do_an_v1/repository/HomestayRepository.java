package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Homestay;

import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.enums.StatusHomestay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {

    boolean existsByTitleAndHost(String title, Host host);

    Page<Homestay> findByStatusHomestay(StatusHomestay statusHomestay, Pageable pageable);


    @Query("""
    SELECT DISTINCT h
    FROM Homestay h
        JOIN h.listPersonHomestay ph
        JOIN ph.person p
    WHERE h.address = :address
      AND (
           (p.type = 'Adults' AND :numAults > 0 AND ph.quantity >= :numAdults)
        OR (p.type = 'Children' AND :numchilden > 0 AND ph.quantity >= :numChildren)
        OR (p.type = 'Baby'  AND :numBaby > 0   AND ph.quantity >= :numBaby)
      )
      AND NOT EXISTS (
          SELECT 1
          FROM Homestay h1
              JOIN h1.listHomestayDailyPrice hdp
              JOIN hdp.pricePerDay ppd
          WHERE h1 = h
            AND hdp.isBooked = true
            AND ppd.day BETWEEN :begin AND :end
      )
    """)
    List<Homestay> findHomestay(
            @Param("address") String address,
            @Param("numAdults") Integer numAdults,
            @Param("numChildren") Integer numChildren,
            @Param("numBaby") Integer numBaby,
            @Param("begin") String begin,
            @Param("end") String end
    );



}
