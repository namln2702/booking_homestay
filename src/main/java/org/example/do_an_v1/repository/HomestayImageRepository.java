package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.HomestayImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.example.do_an_v1.entity.Homestay;
import java.util.List;

@Repository
public interface HomestayImageRepository extends JpaRepository<HomestayImage, Long> {

    List<HomestayImage> findByHomestay(Homestay homestay);
}
