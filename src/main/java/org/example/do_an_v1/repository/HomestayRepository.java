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

    @Query(value = """
        SELECT h.*
        FROM tbl_homestays h
        JOIN tbl_addresses a ON a.id = h.address_id
        WHERE h.status = 'ACTIVE'
          AND (
                CAST(:city AS text) IS NULL OR CAST(:city AS text) = ''
             OR unaccent(lower(a.city)) = unaccent(lower(CAST(:city AS text)))
          )
          AND (
                CAST(:state AS text) IS NULL OR CAST(:state AS text) = ''
             OR unaccent(lower(a.state)) = unaccent(lower(CAST(:state AS text)))
          )
          AND (
                CAST(:keyword AS text) IS NULL OR CAST(:keyword AS text) = ''
             OR unaccent(lower(COALESCE(h.title, ''))) % unaccent(lower(COALESCE(CAST(:keyword AS text), '')))
             OR unaccent(lower(COALESCE(a.city, ''))) % unaccent(lower(COALESCE(CAST(:keyword AS text), '')))
             OR unaccent(lower(COALESCE(a.state, ''))) % unaccent(lower(COALESCE(CAST(:keyword AS text), '')))
             OR unaccent(lower(COALESCE(a.address_line, ''))) % unaccent(lower(COALESCE(CAST(:keyword AS text), '')))
          )
          AND (
                CAST(:numAdults AS integer) IS NULL OR CAST(:numAdults AS integer) <= 0
             OR EXISTS (
                    SELECT 1
                    FROM tbl_person_homestay ph
                    JOIN tbl_persons p ON p.id = ph.person_id
                    WHERE ph.homestay_id = h.address_id
                      AND p.type = 'ADULTS'
                      AND ph.quantity >= CAST(:numAdults AS integer)
             )
          )
          AND (
                CAST(:numChildren AS integer) IS NULL OR CAST(:numChildren AS integer) <= 0
             OR EXISTS (
                    SELECT 1
                    FROM tbl_person_homestay ph
                    JOIN tbl_persons p ON p.id = ph.person_id
                    WHERE ph.homestay_id = h.address_id
                      AND p.type = 'CHILDREN'
                      AND ph.quantity >= CAST(:numChildren AS integer)
             )
          )
          AND (
                CAST(:numBaby AS integer) IS NULL OR CAST(:numBaby AS integer) <= 0
             OR EXISTS (
                    SELECT 1
                    FROM tbl_person_homestay ph
                    JOIN tbl_persons p ON p.id = ph.person_id
                    WHERE ph.homestay_id = h.address_id
                      AND p.type = 'BABY'
                      AND ph.quantity >= CAST(:numBaby AS integer)
             )
          )
          AND (
                CAST(:begin AS date) IS NULL OR CAST(:end AS date) IS NULL
             OR NOT EXISTS (
                    SELECT 1
                    FROM tbl_homestay_daily_prices hdp
                    JOIN tbl_price_per_days ppd ON ppd.id = hdp.price_per_day_id
                    WHERE hdp.homestay_id = h.address_id
                      AND hdp.is_booked = true
                      AND ppd.day BETWEEN CAST(:begin AS date) AND CAST(:end AS date)
             )
          )
        ORDER BY
          CASE
              WHEN CAST(:keyword AS text) IS NULL OR CAST(:keyword AS text) = '' THEN 0
              ELSE similarity(
                       unaccent(lower(COALESCE(h.title, ''))),
                       unaccent(lower(COALESCE(CAST(:keyword AS text), '')))
                   )
          END DESC,
          CASE
              WHEN CAST(:keyword AS text) IS NULL OR CAST(:keyword AS text) = '' THEN 0
              ELSE similarity(
                       unaccent(lower(COALESCE(a.city, ''))),
                       unaccent(lower(COALESCE(CAST(:keyword AS text), '')))
                   )
          END DESC,
          h.created_at DESC
        """, nativeQuery = true)
    List<Homestay> searchHomestayAdvanced(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("state") String state,
            @Param("numAdults") Integer numAdults,
            @Param("numChildren") Integer numChildren,
            @Param("numBaby") Integer numBaby,
            @Param("begin") Date begin,
            @Param("end") Date end
    );



}
