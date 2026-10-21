package de.oberamsystems.slm.model;

import java.time.LocalDateTime;

public class LastDoneHabitEntry {
	
	private Long id;
	private LocalDateTime date;
	private String difference;
	
	public LastDoneHabitEntry() {
	}

	public LastDoneHabitEntry(Long id, LocalDateTime date, String difference) {
		super();
		this.id = id;
		this.date = date;
		this.difference = difference;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
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
