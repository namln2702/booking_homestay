package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.repository.projection.CustomerTierStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    /**
     * Tìm tất cả bills của một customer
     */
    List<Bill> findByCustomer(Customer customer);

    /**
     * Tìm bills của customer với status cụ thể
     */
    List<Bill> findByCustomerAndStatus(Customer customer, StatusBill status);

    /**
     * Kiểm tra customer đã booking homestay này chưa
     */
    Optional<Bill> findByHomestayAndCustomer(Homestay homestay, Customer customer);

    /**
     * Tất cả bills của các homestay thuộc về một host
     */
    List<Bill> findByHomestay_Host(Host host);

    /**
     * Đếm số lượng bills của một homestay
     */
    long countByHomestay(Homestay homestay);

    /**
     * Tìm tất cả bills của một homestay
     */
    List<Bill> findByHomestay(Homestay homestay);

    /**
     * Aggregated booking stats for tier calculation (read-only).
     */
    @Query("""
            SELECT
                COALESCE(SUM(CASE WHEN b.status = org.example.do_an_v1.enums.StatusBill.SUCCEED THEN 1 ELSE 0 END), 0) AS successfulBookings,
                COALESCE(SUM(CASE WHEN b.status = org.example.do_an_v1.enums.StatusBill.REFUNDED THEN 1 ELSE 0 END), 0) AS refundedBookings
            FROM Bill b
            WHERE b.customer.id = :customerId
            """)
    CustomerTierStats findTierStatsByCustomerId(@Param("customerId") Long customerId);
    /**
     * Tìm tất cả bills với status cụ thể
     */
    List<Bill> findByStatus(StatusBill status);

    /**
     * Tìm bills theo status và khoảng thời gian createdAt
     */
    List<Bill> findByStatusAndCreatedAtBetween(
            StatusBill status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
