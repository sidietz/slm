package de.oberamsystems.slm.model;

public enum PlugType {
	CCS_TYPE2("CCS Type 2", true),
	CHADEMO("CHAdeMO", true),
	GBT("GB/T", false),
	OTHER("Other", true),
	TESLA("Tesla", true),
	TYPE_1("Type 1", false),
	TYPE_2("Type 2", true),
	UNKNOWN("Unknown", true);

	private final String displayName;
	private final boolean commonInEurope;

	PlugType(String displayName, boolean commonInEurope) {
		this.displayName = displayName;
		this.commonInEurope = commonInEurope;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isCommonInEurope() {
		return commonInEurope;
	}
}
