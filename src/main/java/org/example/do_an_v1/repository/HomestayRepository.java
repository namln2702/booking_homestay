package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.enums.StatusHomestay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {

    boolean existsByTitleAndHost(String title, Host host);

    Page<Homestay> findByStatusHomestay(StatusHomestay statusHomestay, Pageable pageable);
}
