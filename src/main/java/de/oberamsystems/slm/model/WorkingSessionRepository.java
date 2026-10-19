package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingSessionRepository extends JpaRepository<WorkingSession, UUID> {

	List<WorkingSession> findByEmployer(Employer employer);

	List<WorkingSession> findByProject(String project);

	List<WorkingSession> findByBillable(Boolean billable);
}
