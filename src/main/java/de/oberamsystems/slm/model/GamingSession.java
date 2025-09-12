package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
@Table(name = "gaming_session")
public class GamingSession {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;
	@ManyToOne
	@JoinColumn(name="game_id", referencedColumnName = "id")
	private Game game;
	@ManyToOne
	@JoinColumn(name="device_id", referencedColumnName = "id")
	private Device device;
	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable=false)
	private Duration duration;
	
	private String comment;
	
	public GamingSession() {
	}

	public GamingSession(long id, Game game, Device device, LocalDateTime start, LocalDateTime end, Duration duration,
			String comment) {
		super();
		this.id = id;
		this.game = game;
		this.device = device;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.comment = comment;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("GamingSession [id=%s, game=%s, device=%s, start=%s, end=%s, duration=%s, comment=%s]", id,
				game, device, start, end, duration, comment);
	}	
}
