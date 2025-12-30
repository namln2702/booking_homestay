package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Tìm transaction theo orderId (VNPay vnp_TxnRef)
     */
    Optional<Transaction> findByOrderId(String orderId);

    /**
     * Tìm tất cả transaction REFUND đang chờ xử lý
     */
    List<Transaction> findByTransactionTypeAndStatus(
            org.example.do_an_v1.enums.TypeTransaction transactionType,
            org.example.do_an_v1.enums.StatusTransaction status
    );

    /**
     * Tìm tất cả transaction theo danh sách type và status
     */
    List<Transaction> findByTransactionTypeInAndStatus(
            java.util.List<org.example.do_an_v1.enums.TypeTransaction> transactionTypes,
            org.example.do_an_v1.enums.StatusTransaction status
    );

    /**
     * Tìm tất cả transaction theo danh sách type và status với JOIN FETCH để eager load
     * Bill -> Homestay -> Host và Address để tránh N+1 query problem
     */
    @Query("""
        SELECT DISTINCT t
        FROM Transaction t
        JOIN FETCH t.bill b
        JOIN FETCH b.homestay h
        JOIN FETCH h.host host
        JOIN FETCH h.address a
        WHERE t.transactionType IN :transactionTypes
          AND t.status = :status
    """)
    List<Transaction> findByTransactionTypeInAndStatusWithJoins(
            @Param("transactionTypes") java.util.List<org.example.do_an_v1.enums.TypeTransaction> transactionTypes,
            @Param("status") org.example.do_an_v1.enums.StatusTransaction status
    );
}

