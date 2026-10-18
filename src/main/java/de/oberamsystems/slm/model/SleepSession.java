package de.oberamsystems.slm.model;

import java.time.Duration;

import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
@Table(name = "sleep_session")
@DiscriminatorValue("SLEEP")
public class SleepSession extends Session {

	@Column(name = "is_nap")
	private Boolean isNap = false;

	@Type(PostgreSQLIntervalType.class)
	@Column(name = "deep_sleep_duration", columnDefinition = "interval")
	private Duration deepSleepDuration;

	@Type(PostgreSQLIntervalType.class)
	@Column(name = "rem_sleep_duration", columnDefinition = "interval")
	private Duration remSleepDuration;

	@Column(name = "awake_count")
	private Integer awakeCount = 0;

	public SleepSession() {
		super();
	}

	public Boolean getIsNap() {
		return isNap;
	}

	public void setIsNap(Boolean isNap) {
		this.isNap = isNap;
	}

	public Duration getDeepSleepDuration() {
		return deepSleepDuration;
	}

	public void setDeepSleepDuration(Duration deepSleepDuration) {
		this.deepSleepDuration = deepSleepDuration;
	}

	public Duration getRemSleepDuration() {
		return remSleepDuration;
	}

	public void setRemSleepDuration(Duration remSleepDuration) {
		this.remSleepDuration = remSleepDuration;
	}

	public Integer getAwakeCount() {
		return awakeCount;
	}

	public void setAwakeCount(Integer awakeCount) {
		this.awakeCount = awakeCount;
	}

	@Transient
	public java.time.LocalDateTime getGotobed() {
		return getStart();
	}

	public void setGotobed(java.time.LocalDateTime gotobed) {
		setStart(gotobed);
	}

	@Transient
	public java.time.LocalDateTime getGetup() {
		return getEnd();
	}

	public void setGetup(java.time.LocalDateTime getup) {
		setEnd(getup);
	}

	@Transient
	public Duration getSleeptime() {
		return getDuration();
	}

	@Override
	public String toString() {
		return String.format("SleepSession [id=%s, isNap=%s, awakeCount=%s, session=%s]", getId(), isNap, awakeCount,
				super.toString());
	}
}
