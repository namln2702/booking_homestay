package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.CustomerBookingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerBookingInfoRepository extends JpaRepository<CustomerBookingInfo, Long> {

    Optional<CustomerBookingInfo> findByPhoneNumber(String phoneNumber);
}
