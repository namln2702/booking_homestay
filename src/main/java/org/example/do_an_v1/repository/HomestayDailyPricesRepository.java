package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface HomestayDailyPricesRepository extends JpaRepository<HomestayDailyPrice, Long> {

    @Query("""
        SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM HomestayDailyPrice hdp
                JOIN hdp.pricePerDay ppd
                WHERE ppd.day BETWEEN :start AND :end
                  AND hdp.isBooked = FALSE
                  AND hdp.homestay.id = :idHomestay
        ) THEN TRUE ELSE FALSE END
""")
    Boolean checkHomestayAvailability(Long idHomestay, String start, String end);

    /**
     * Tìm các HomestayDailyPrice theo homestay và khoảng thời gian
     */
    @Query("""
        SELECT hdp
        FROM HomestayDailyPrice hdp
        JOIN hdp.pricePerDay ppd
        WHERE hdp.homestay.id = :homestayId
          AND ppd.day >= :startDate
          AND ppd.day < :endDate
        ORDER BY ppd.day ASC
""")
    List<HomestayDailyPrice> findByHomestayAndDateRange(
            @Param("homestayId") Long homestayId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * Tìm HomestayDailyPrice theo homestay và pricePerDay
     */
    @Query("""
        SELECT hdp
        FROM HomestayDailyPrice hdp
        WHERE hdp.homestay.id = :homestayId
          AND hdp.pricePerDay.id = :pricePerDayId
""")
    List<HomestayDailyPrice> findByHomestayAndPricePerDay(
            @Param("homestayId") Long homestayId,
            @Param("pricePerDayId") Long pricePerDayId
    );

}
