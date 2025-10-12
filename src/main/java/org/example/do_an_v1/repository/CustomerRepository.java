package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByUser(User user);

}
