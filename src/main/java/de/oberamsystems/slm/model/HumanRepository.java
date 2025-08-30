package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HumanRepository extends JpaRepository<Human, Long> {
	List<Human> findAllByOrderByDaysUntilBirthdayAsc();
}
