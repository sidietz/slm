package de.oberamsystems.slm.model;

import java.util.Arrays;
import java.util.Optional;

public enum CarExpenseType {
	MAINTENANCE(1, "Maintenance", "Wartung", "bi-wrench-adjustable", "badge-warning", false),
	REPAIR(2, "Repair", "Reparatur", "bi-tools", "badge-danger", false),
	TIRE_CHANGE(3, "Tire Change", "Reifenwechsel", "bi-disc", "badge-info", false),
	OIL_CHANGE(4, "Oil Change", "Ölwechsel", "bi-droplet-half", "badge-dark", false),
	INSURANCE(5, "Insurance", "Versicherung", "bi-shield-check", "badge-primary", false),
	TAX(6, "Tax", "Steuer", "bi-receipt", "badge-secondary", false),
	INSPECTION(7, "Inspection / TÜV", "HU/AU (TÜV)", "bi-clipboard-check", "badge-success", false),
	TUNING(8, "Tuning", "Tuning", "bi-speedometer", "badge-info", false),
	ACCESSORIES(9, "Accessories", "Zubehör", "bi-box-seam", "badge-secondary", false),
	PURCHASE_PRICE(10, "Purchase Price", "Kaufpreis", "bi-cash-stack", "badge-dark", false),
	OTHER(11, "Other", "Sonstiges", "bi-three-dots", "badge-light text-dark", false),
	CARE(12, "Care & Wash", "Wagenpflege", "bi-stars", "badge-info", false),
	LEASING_RATE(13, "Leasing Rate", "Leasingrate", "bi-calendar-range", "badge-warning", false),
	REGISTRATION(14, "Registration", "Zulassung/Ummeldung", "bi-card-heading", "badge-primary", false),
	FINANCING_RATE(15, "Financing Rate", "Finanzierungsrate", "bi-bank", "badge-warning", false),
	REFUND(16, "Refund / Reimbursement", "Erstattung", "bi-arrow-down-left-circle", "badge-success", true),
	FINE(17, "Traffic Fine", "Bußgeld", "bi-exclamation-octagon", "badge-danger", false),
	PARKING_FEE(18, "Parking Fee", "Parkgebühr", "bi-p-circle", "badge-secondary", false),
	TOLL(19, "Toll", "Maut", "bi-signpost", "badge-secondary", false),
	SPARE_PARTS(20, "Spare Parts", "Ersatzteile", "bi-gear", "badge-warning", false),
	CHARGING_BASE_FEE(21, "Charging Base Fee", "Ladegrundgebühr", "bi-lightning-charge", "badge-info", false),
	DIGITAL_SERVICES(22, "Digital Services", "Digitale Dienste", "bi-wifi", "badge-primary", false),
	GHG_QUOTA(23, "GHG Quota", "THG-Prämie", "bi-tree", "badge-success", true);

	private final int spritmonitorId;
	private final String displayName;
	private final String germanName;
	private final String icon;
	private final String badgeClass;
	private final boolean credit;

	CarExpenseType(int spritmonitorId, String displayName, String germanName, String icon, String badgeClass, boolean credit) {
		this.spritmonitorId = spritmonitorId;
		this.displayName = displayName;
		this.germanName = germanName;
		this.icon = icon;
		this.badgeClass = badgeClass;
		this.credit = credit;
	}

	public int getSpritmonitorId() {
		return spritmonitorId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getGermanName() {
		return germanName;
	}

	public String getIcon() {
		return icon;
	}

	public String getBadgeClass() {
		return badgeClass;
	}

	public boolean isCredit() {
		return credit;
	}

	public static Optional<CarExpenseType> fromSpritmonitorId(int id) {
		return Arrays.stream(values())
				.filter(type -> type.getSpritmonitorId() == id)
				.findFirst();
	}
}
