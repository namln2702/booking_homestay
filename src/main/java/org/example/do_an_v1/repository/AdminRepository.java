package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByUser(User user);
}