package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Complaint;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Homestay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByBill(Bill bill);
    List<Complaint> findByBill_Customer(Customer customer);
    Optional<Complaint> findTopByBillOrderByCreatedAtDesc(Bill bill);
    
    default List<Complaint> findByBillId(Long billId) {
        return findAll().stream()
                .filter(c -> c.getBill() != null && c.getBill().getId().equals(billId))
                .toList();
    }

    /**
     * Đếm số lượng complaints của một homestay (qua bills)
     */
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.bill.homestay = :homestay")
    long countByHomestay(@Param("homestay") Homestay homestay);
}
