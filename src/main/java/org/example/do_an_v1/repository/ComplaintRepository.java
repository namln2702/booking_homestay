package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByBill(Bill bill);
    
    default List<Complaint> findByBillId(Long billId) {
        return findAll().stream()
                .filter(c -> c.getBill() != null && c.getBill().getId().equals(billId))
                .toList();
    }
}
