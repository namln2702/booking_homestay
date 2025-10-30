package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Homestay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {


    @Query(value = "select h  from Homestay h " +
            "inner join h.listPersonHomestay ph " +
            "inner join ph.person p " +
            "where h.address = ? and " +
                "case " +
                "when p.type = 'Adults' and ph.quantity >= ? then true " +
                "when p.type = 'Children' and ph.quantity >= ? then true " +
                "when p.type = 'Baby' and ph.quantity >= ? then true " +
                "end " +
            "and 0 = (select count(*) from Homestay h1 " +
            "inner join h1.listHomestayDailyPrice as hdp " +
            "inner join hdp.pricePerDay ppd " +
            "where hdp.isBooked = true and ppd.day >= ? and ppd.day <= ?" +
            ")"

    )
    List<Homestay> findHomestay(String address, Integer numberAdults, Integer numberChildren,
                                Integer numberBaby, String begin, String end
                                );


}
