package de.oberamsystems.slm.model;

public enum EventType {
	BIRTHDAY("Birthday", "bi-cake2", "text-primary"),
	MEMORIAL("Memorial / Remembrance", "bi-candle", "text-secondary"),
	WEDDING_ANNIVERSARY("Wedding Anniversary", "bi-heart-fill", "text-danger"),
	RELATIONSHIP_ANNIVERSARY("Relationship Anniversary", "bi-stars", "text-warning"),
	WORK_ANNIVERSARY("Career Milestone", "bi-briefcase-fill", "text-info"),
	CUSTOM("Custom Anniversary", "bi-calendar-event", "text-success");

	private final String displayName;
	private final String iconClass;
	private final String colorClass;

	EventType(String displayName, String iconClass, String colorClass) {
		this.displayName = displayName;
		this.iconClass = iconClass;
		this.colorClass = colorClass;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getIconClass() {
		return iconClass;
	}

	public String getColorClass() {
		return colorClass;
	}
}
