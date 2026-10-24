package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.OffsetDateTime;

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

	@Type(PostgreSQLIntervalType.class)
	@Column(name = "light_sleep_duration", columnDefinition = "interval")
	private Duration lightSleepDuration;

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

	public Duration getLightSleepDuration() {
		return lightSleepDuration;
	}

	public void setLightSleepDuration(Duration lightSleepDuration) {
		this.lightSleepDuration = lightSleepDuration;
	}

	public Integer getAwakeCount() {
		return awakeCount;
	}

	public void setAwakeCount(Integer awakeCount) {
		this.awakeCount = awakeCount;
	}

	@Transient
	public OffsetDateTime getGotobed() {
		return getStart();
	}

	public void setGotobed(OffsetDateTime gotobed) {
		setStart(gotobed);
	}

	@Transient
	public OffsetDateTime getGetup() {
		return getEnd();
	}

	public void setGetup(OffsetDateTime getup) {
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
