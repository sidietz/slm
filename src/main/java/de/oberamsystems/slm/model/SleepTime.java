package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;

import jakarta.persistence.*;

/**
 * Entity for legacy standalone sleep time records.
 *
 * @deprecated since 0.0.3-rc9, scheduled for removal in future major release.
 *             Use {@link SleepSession} instead.
 */
@Deprecated(since = "0.0.3-rc9", forRemoval = true)
@Entity
@Table(name = "sleeptime")
public class SleepTime {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "gotobed", columnDefinition = "TIMESTAMP")
	private LocalDateTime gotobed;
	
	@Column(name = "standup", columnDefinition = "TIMESTAMP")
	private LocalDateTime getup;

	@Type(PostgreSQLIntervalType.class)
	@Column(name = "sleeptime", columnDefinition = "interval", insertable = false, updatable = false)
	@org.hibernate.annotations.Generated(event = { org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE })
	private Duration sleeptime;
	

	public SleepTime() {
	}
	
	public SleepTime(Long id, LocalDateTime gotobed, LocalDateTime getup, Duration sleeptime) {
		this.id = id;
		this.gotobed = gotobed;
		this.getup = getup;
		this.sleeptime = sleeptime;
	}

	public LocalDateTime getGotobed() {
		return gotobed;
	}

	public void setGotobed(LocalDateTime gotobed) {
		this.gotobed = gotobed;
	}
	
	public LocalDateTime getGetup() {
		return getup;
	}

	public void setGetup(LocalDateTime getup) {
		this.getup = getup;
	}

	public Duration getSleeptime() {
		return sleeptime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.format("SleepTime [id=%s, gotobed=%s, getup=%s, sleeptime=%s]", id, gotobed, getup,
				sleeptime);
	}	
}
