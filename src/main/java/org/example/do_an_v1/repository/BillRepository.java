package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.enums.StatusBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
