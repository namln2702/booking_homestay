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

import java.util.Date;
import java.util.List;


@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {

    boolean existsByTitleAndHost(String title, Host host);

    /**
     * Lấy tất cả homestay của một host
     */
    List<Homestay> findByHost(Host host);

    Page<Homestay> findByStatusHomestay(StatusHomestay statusHomestay, Pageable pageable);


    @Query("""
        SELECT DISTINCT h
        FROM Homestay h
        WHERE h.statusHomestay = 'ACTIVE'
          AND (:city IS NULL OR h.address.city = :city)
          AND (:state IS NULL OR h.address.state = :state)
          AND (
               (:numAdults IS NULL OR :numAdults <= 0)
            OR EXISTS (
                SELECT 1 FROM PersonHomestay ph2
                JOIN ph2.person p2
                WHERE ph2.homestay = h
                  AND p2.type = 'ADULTS'
                  AND ph2.quantity >= :numAdults
            )
          )
          AND (
               (:numChildren IS NULL OR :numChildren <= 0)
            OR EXISTS (
                SELECT 1 FROM PersonHomestay ph3
                JOIN ph3.person p3
                WHERE ph3.homestay = h
                  AND p3.type = 'CHILDREN'
                  AND ph3.quantity >= :numChildren
            )
          )
          AND (
               (:numBaby IS NULL OR :numBaby <= 0)
            OR EXISTS (
                SELECT 1 FROM PersonHomestay ph4
                JOIN ph4.person p4
                WHERE ph4.homestay = h
                  AND p4.type = 'BABY'
                  AND ph4.quantity >= :numBaby
            )
          )
    """)
    List<Homestay> findHomestay(
            @Param("city") String city,
            @Param("state") String state,
            @Param("numAdults") Integer numAdults,
            @Param("numChildren") Integer numChildren,
            @Param("numBaby") Integer numBaby
    );

    @Query("""
        SELECT DISTINCT h
        FROM Homestay h
        WHERE h.statusHomestay = 'ACTIVE'
          AND (:city IS NULL OR h.address.city = :city)
          AND (:state IS NULL OR h.address.state = :state)
          AND (
               (:numAdults IS NULL OR :numAdults <= 0)
            OR EXISTS (
                SELECT 1 FROM PersonHomestay ph2
                JOIN ph2.person p2
                WHERE ph2.homestay = h
                  AND p2.type = 'ADULTS'
                  AND ph2.quantity >= :numAdults
            )
          )
          AND (
               (:numChildren IS NULL OR :numChildren <= 0)
            OR EXISTS (
                SELECT 1 FROM PersonHomestay ph3
                JOIN ph3.person p3
                WHERE ph3.homestay = h
                  AND p3.type = 'CHILDREN'
                  AND ph3.quantity >= :numChildren
            )
          )
          AND (
               (:numBaby IS NULL OR :numBaby <= 0)
            OR EXISTS (
                SELECT 1 FROM PersonHomestay ph4
                JOIN ph4.person p4
                WHERE ph4.homestay = h
                  AND p4.type = 'BABY'
                  AND ph4.quantity >= :numBaby
            )
          )
          AND NOT EXISTS (
              SELECT 1
              FROM HomestayDailyPrice hdp
              JOIN hdp.pricePerDay ppd
              WHERE hdp.homestay = h
                AND hdp.isBooked = true
                AND ppd.day BETWEEN :begin AND :end
          )
    """)
    List<Homestay> findHomestayWithDateRange(
            @Param("city") String city,
            @Param("state") String state,
            @Param("numAdults") Integer numAdults,
            @Param("numChildren") Integer numChildren,
            @Param("numBaby") Integer numBaby,
            @Param("begin") Date begin,
            @Param("end") Date end
    );



}
