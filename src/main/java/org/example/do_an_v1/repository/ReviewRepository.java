package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Tìm review của customer cho một homestay
     */
    Optional<Review> findByHomestayAndCustomer(Homestay homestay, Customer customer);

    /**
     * Lấy tất cả reviews của một homestay
     */
    List<Review> findByHomestay(Homestay homestay);

    /**
     * Lấy tất cả reviews của một customer (mới nhất trước)
     */
    List<Review> findByCustomerOrderByCreatedAtDesc(Customer customer);
}
