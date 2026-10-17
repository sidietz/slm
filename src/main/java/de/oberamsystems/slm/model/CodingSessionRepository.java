package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingSessionRepository extends JpaRepository<CodingSession, String> {

	List<CodingSession> findByProject(String project);

	List<CodingSession> findByRepository(String repository);
}
