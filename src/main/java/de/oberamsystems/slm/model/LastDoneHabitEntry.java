package de.oberamsystems.slm.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import de.oberamsystems.slm.config.TimeZoneConfig;

public class LastDoneHabitEntry {
	
	private Long id;
	private OffsetDateTime date;
	private String difference;
	
	public LastDoneHabitEntry() {
	}

	public LastDoneHabitEntry(Long id, Object date, String difference) {
		super();
		this.id = id;
		if (date instanceof Instant instant) {
			this.date = instant.atZone(TimeZoneConfig.getZoneId()).toOffsetDateTime();
		} else if (date instanceof OffsetDateTime odt) {
			this.date = odt.atZoneSameInstant(TimeZoneConfig.getZoneId()).toOffsetDateTime();
		} else if (date instanceof LocalDateTime ldt) {
			this.date = ldt.atZone(TimeZoneConfig.getZoneId()).toOffsetDateTime();
		} else if (date instanceof java.sql.Timestamp ts) {
			this.date = ts.toInstant().atZone(TimeZoneConfig.getZoneId()).toOffsetDateTime();
		} else {
			this.date = null;
		}
		this.difference = difference;
	}

	public OffsetDateTime getDate() {
		return date;
	}

	public void setDate(OffsetDateTime date) {
		this.date = date;
	}

	public String getDifference() {
		return difference;
	}

	public void setDifference(String difference) {
		this.difference = difference;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
