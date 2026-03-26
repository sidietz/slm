package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelfieRepository extends JpaRepository<Selfie, Long> {
    Selfie findByDate(LocalDate date);
}
