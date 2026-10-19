package de.oberamsystems.slm.model;

public enum TireType {
	SUMMER("Summer Tires", "bi-sun-fill", "text-warning"),
	WINTER("Winter Tires", "bi-snow", "text-info"),
	ALL_SEASON("All-Season Tires", "bi-cloud-sun-fill", "text-secondary");

	private final String displayName;
	private final String iconClass;
	private final String colorClass;

	TireType(String displayName, String iconClass, String colorClass) {
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

	public static TireType fromSpritmonitorCode(Integer code) {
		if (code == null) return null;
		return switch (code) {
			case 1 -> SUMMER;
			case 2 -> WINTER;
			case 3 -> ALL_SEASON;
			default -> null;
		};
	}
}
