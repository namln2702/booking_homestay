package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

}
