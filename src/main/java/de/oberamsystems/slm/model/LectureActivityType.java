package de.oberamsystems.slm.model;

public enum LectureActivityType {
	LECTURE("Lecture", "bi-mortarboard-fill", "bg-primary-subtle text-primary"),
	PREPARATION("Preparation", "bi-book-half", "bg-info-subtle text-info"),
	LEARNING("Self-Study / Learning", "bi-lightbulb-fill", "bg-success-subtle text-success"),
	HOMEWORK("Homework / Exercises", "bi-pencil-square", "bg-warning-subtle text-warning"),
	LAB("Laboratory / Practical", "bi-funnel-fill", "bg-danger-subtle text-danger"),
	PROJECT("Project Work", "bi-code-square", "bg-secondary-subtle text-secondary"),
	PRESENTATION("Presentation", "bi-easel-fill", "bg-dark-subtle text-dark");

	private final String displayName;
	private final String iconClass;
	private final String badgeClass;

	LectureActivityType(String displayName, String iconClass, String badgeClass) {
		this.displayName = displayName;
		this.iconClass = iconClass;
		this.badgeClass = badgeClass;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getIconClass() {
		return iconClass;
	}

	public String getBootstrapIcon() {
		return iconClass;
	}

	public String getBadgeClass() {
		return badgeClass;
	}

	public String getBadgeCssClass() {
		return badgeClass;
	}
}

