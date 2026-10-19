package de.oberamsystems.slm.model;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningSessionRepository extends JpaRepository<LearningSession, UUID> {
}
