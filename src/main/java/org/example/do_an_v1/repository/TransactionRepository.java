package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Tìm transaction theo bill
     */
    List<Transaction> findByBill(Bill bill);

    /**
     * Tìm transaction theo bill ID
     */
    Optional<Transaction> findByBillId(Long billId);
}

