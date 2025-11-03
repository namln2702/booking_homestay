package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {

}
