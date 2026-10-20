package de.oberamsystems.slm.model;

public enum StationType {
	GAS_STATION("Gas Station", "bi-fuel-pump", "badge bg-primary"),
	CHARGING_STATION("EV Charging Station", "bi-lightning-charge-fill", "badge bg-success"),
	H2_STATION("Hydrogen Station", "bi-droplet-half", "badge bg-info text-dark"),
	OTHER("Other Station", "bi-geo-alt", "badge bg-secondary");

	private final String displayName;
	private final String iconClass;
	private final String badgeClass;

	StationType(String displayName, String iconClass, String badgeClass) {
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
}
