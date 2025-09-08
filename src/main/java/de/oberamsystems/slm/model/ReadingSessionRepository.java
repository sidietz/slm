package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

	public ReadingSession findFirstByOrderByStartDesc();

	public List<ReadingSession> findAllByOrderByStart();
}
