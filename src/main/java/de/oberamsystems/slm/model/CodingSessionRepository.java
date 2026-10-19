package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingSessionRepository extends JpaRepository<CodingSession, UUID> {

	List<CodingSession> findByProject(String project);

	List<CodingSession> findByRepository(String repository);
}
