package de.oberamsystems.slm.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {
	public DiaryEntry findFirstByOrderByDateDesc();

	List<DiaryEntry> findByDateBetween(LocalDate fromDate, LocalDate toDate);
	List<DiaryEntry> findByDateBetween(LocalDate fromDate, LocalDate toDate, Sort sort);

	List<DiaryEntry> findByDateAfter(LocalDate ldt);
	List<DiaryEntry> findByDateAfter(LocalDate ldt, Sort sort);
	
	@Query("SELECT MIN(g.date) FROM DiaryEntry g")
    LocalDate findMinDate();
    @Query("SELECT MAX(g.date) FROM DiaryEntry g")
    LocalDate findMaxDate();
}
