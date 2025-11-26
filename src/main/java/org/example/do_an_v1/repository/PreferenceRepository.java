package org.example.do_an_v1.repository;

import org.example.do_an_v1.entity.Preference;
import org.example.do_an_v1.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {
}
