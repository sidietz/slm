package de.oberamsystems.slm.model;

public enum FuelingType {
	FULL("Full Tank", "bi-circle-fill", "badge bg-success"),
	PARTIAL("Partial Tank", "bi-pie-chart-fill", "badge bg-warning text-dark"),
	INITIAL("First / Initial Tank", "bi-flag-fill", "badge bg-info text-dark");

	private final String displayName;
	private final String iconClass;
	private final String badgeClass;

	FuelingType(String displayName, String iconClass, String badgeClass) {
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

	public String getBadgeClass() {
		return badgeClass;
	}

	public static FuelingType fromSpritmonitorCode(Integer code) {
		if (code == null) return FULL;
		return switch (code) {
			case 2 -> PARTIAL;
			case 3 -> INITIAL;
			default -> FULL;
		};
	}
}
