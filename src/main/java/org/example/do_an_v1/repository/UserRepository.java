package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
