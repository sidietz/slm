package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HabitEntryRepository extends JpaRepository<HabitEntry, Long> {

	@Query(value = "SELECT habit, last_done, CAST(last_done - LAG(last_done) OVER (ORDER BY last_done ASC) AS TEXT) as diff FROM habit_entry WHERE habit = ?1 ORDER BY last_done DESC;", nativeQuery = true)
	List<LastDoneHabitEntry> lastDoneHabitEntryById(long id);
}
