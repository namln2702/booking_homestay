package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Person;
import org.example.do_an_v1.enums.TypePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByType(TypePerson type);
}
