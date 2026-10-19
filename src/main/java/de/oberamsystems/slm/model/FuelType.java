package de.oberamsystems.slm.model;

public enum FuelType {
	DIESEL("Diesel", "bi-fuel-pump"),
	SUPER_E10("Super E10", "bi-fuel-pump"),
	SUPER_E5("Super E5", "bi-fuel-pump"),
	SUPER_PLUS("Super Plus", "bi-fuel-pump"),
	PREMIUM_DIESEL("Premium Diesel", "bi-fuel-pump-fill"),
	LPG("Autogas (LPG)", "bi-fire"),
	CNG("Erdgas (CNG)", "bi-fire"),
	ELECTRIC("Electric (EV)", "bi-lightning-charge"),
	HYDROGEN("Hydrogen (H2)", "bi-droplet"),
	OTHER("Other Fuel", "bi-fuel-pump");

	private final String displayName;
	private final String iconClass;

	FuelType(String displayName, String iconClass) {
		this.displayName = displayName;
		this.iconClass = iconClass;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getIconClass() {
		return iconClass;
	}

	public static FuelType fromSpritmonitorCode(Integer code) {
		if (code == null) return null;
		return switch (code) {
			case 1 -> DIESEL;
			case 2 -> SUPER_E5;
			case 5 -> CNG;
			case 6 -> LPG;
			case 7 -> SUPER_E10;
			case 8 -> SUPER_PLUS;
			case 9 -> ELECTRIC;
			case 11 -> PREMIUM_DIESEL;
			case 14 -> HYDROGEN;
			default -> OTHER;
		};
	}
}
