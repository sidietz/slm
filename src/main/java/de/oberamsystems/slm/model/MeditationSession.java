package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "meditation_session")
@DiscriminatorValue("MEDITATION")
public class MeditationSession extends Session {

	public MeditationSession() {
		super();
	}

	@Override
	public String toString() {
		return String.format("MeditationSession [id=%s, session=%s]", getId(), super.toString());
	}
}
