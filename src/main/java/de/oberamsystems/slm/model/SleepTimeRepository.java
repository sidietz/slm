package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;

/**
 * Repository for legacy SleepTime entity.
 *
 * @deprecated since 0.0.3-rc9, scheduled for removal in future major release.
 */
@Deprecated(since = "0.0.3-rc9", forRemoval = true)
public interface SleepTimeRepository extends JpaRepository<SleepTime, Long> {
	public List<SleepTime> findByGotobedBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
	public List<SleepTime> findByGotobedBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(t.gotobed) FROM SleepTime t")
    OffsetDateTime findMinDate();
    @Query("SELECT MAX(t.getup) FROM SleepTime t")
    OffsetDateTime findMaxDate();
}
