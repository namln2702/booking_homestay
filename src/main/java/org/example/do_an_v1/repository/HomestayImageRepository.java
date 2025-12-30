package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.HomestayImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.example.do_an_v1.entity.Homestay;
import java.util.List;

@Repository
public interface HomestayImageRepository extends JpaRepository<HomestayImage, Long> {

    List<HomestayImage> findByHomestay(Homestay homestay);

    /**
     * Lấy tất cả images của nhiều homestays cùng lúc (batch load để tránh N+1 query)
     */
    List<HomestayImage> findByHomestayIn(java.util.List<Homestay> homestays);
}
