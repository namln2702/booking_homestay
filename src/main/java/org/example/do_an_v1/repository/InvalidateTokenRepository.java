package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.InvalidateToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InvalidateTokenRepository extends JpaRepository<InvalidateToken, String> {


}
