package de.oberamsystems.slm.model;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamingSessionRepository extends JpaRepository<GamingSession, UUID> {
}
