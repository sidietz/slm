package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, UUID> {

	public ReadingSession findFirstByOrderByStartDesc();

	public List<ReadingSession> findAllByOrderByStart();
}
