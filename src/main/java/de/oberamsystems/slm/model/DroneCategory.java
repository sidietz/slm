package de.oberamsystems.slm.model;

public enum DroneCategory {
    C0("C0 (< 250g, A1/A3)"),
    C1("C1 (< 900g, A1/A3)"),
    C2("C2 (< 4kg, A2/A3)"),
    C3("C3 (< 25kg, A3)"),
    C4("C4 (< 25kg, Model Aircraft / A3)");

    private final String displayName;

    DroneCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
