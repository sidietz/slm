package de.oberamsystems.slm.model;

public enum DrivingStyle {
	ECONOMICAL("Economical", "bi-leaf-fill", "text-success"),
	NORMAL("Normal", "bi-speedometer", "text-primary"),
	FAST("Fast / Dynamic", "bi-lightning-fill", "text-danger");

	private final String displayName;
	private final String iconClass;
	private final String colorClass;

	DrivingStyle(String displayName, String iconClass, String colorClass) {
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

	public static DrivingStyle fromSpritmonitorCode(Integer code) {
		if (code == null) return null;
		return switch (code) {
			case 1 -> ECONOMICAL;
			case 2 -> NORMAL;
			case 3 -> FAST;
			default -> null;
		};
	}
}
