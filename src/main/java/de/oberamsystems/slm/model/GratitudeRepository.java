package de.oberamsystems.slm.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GratitudeRepository extends JpaRepository<Gratitude, Long> {
	public Gratitude findFirstByOrderByDateDesc();

	List<Gratitude> findByDateBetween(LocalDate fromDate, LocalDate toDate);
	List<Gratitude> findByDateBetween(LocalDate fromDate, LocalDate toDate, Sort sort);

	List<Gratitude> findByDateAfter(LocalDate ldt);
	List<Gratitude> findByDateAfter(LocalDate ldt, Sort sort);
	
	@Query("SELECT MIN(g.date) FROM Gratitude g")
    LocalDate findMinDate();
    @Query("SELECT MAX(g.date) FROM Gratitude g")
    LocalDate findMaxDate();
}
