package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sport_session")
@DiscriminatorValue("SPORT")
public class SportSession extends Session {

	@ManyToOne
	@JoinColumn(name = "type", referencedColumnName = "id")
	private SportType type;

	public SportSession() {
		super();
	}

	public SportSession(SportType type) {
		super();
		this.type = type;
	}

	public SportType getType() {
		return type;
	}

	public void setType(SportType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("SportSession [id=%s, type=%s, session=%s]", getId(), type, super.toString());
	}
}
