package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.CustomerBookingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerBookingInfoRepository extends JpaRepository<CustomerBookingInfo, Long> {

}
