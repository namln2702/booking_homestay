package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.ConfirmEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ConfirmEmailRepository extends JpaRepository<ConfirmEmail, Integer> {

    ConfirmEmail findByEmailAndCode(String email, String code);
}
