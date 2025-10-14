package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.ConfirmEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ConfirmEmailRepository extends JpaRepository<ConfirmEmail, Integer> {


    @Query(value = "select ce from ConfirmEmail ce where ce.email = :email and ce.code = :code order by ce.cratedAt desc limit 1")
    ConfirmEmail findByEmailAndCode(@Param("email") String email,@Param("code") String code);
}
