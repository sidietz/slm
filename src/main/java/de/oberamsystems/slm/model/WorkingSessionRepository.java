package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingSessionRepository extends JpaRepository<WorkingSession, String> {

	List<WorkingSession> findByEmployer(Employer employer);

	List<WorkingSession> findByProject(String project);

	List<WorkingSession> findByBillable(Boolean billable);
}
