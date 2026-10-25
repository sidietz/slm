package de.oberamsystems.slm.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "drone")
public class Drone {

    @Id
    @GeneratedValue
    @UuidGenerator(style = Style.VERSION_7)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "model_name")
    private String modelName;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "category", nullable = false)
    private DroneCategory category;

    @Column(name = "weight_grams", nullable = false)
    private Integer weightGrams;

    @Column(name = "buy_date", columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate buyDate;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "uas_operator_id")
    private String uasOperatorId;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "drone", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DroneFlight> flights = new ArrayList<>();

    public Drone() {
    }

    public Drone(String name, String modelName, DroneCategory category, Integer weightGrams, LocalDate buyDate) {
        this.name = name;
        this.modelName = modelName;
        this.category = category;
        this.weightGrams = weightGrams;
        this.buyDate = buyDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public DroneCategory getCategory() {
        return category;
    }

    public void setCategory(DroneCategory category) {
        this.category = category;
    }

    public Integer getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(Integer weightGrams) {
        this.weightGrams = weightGrams;
    }

    public LocalDate getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(LocalDate buyDate) {
        this.buyDate = buyDate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getUasOperatorId() {
        return uasOperatorId;
    }

    public void setUasOperatorId(String uasOperatorId) {
        this.uasOperatorId = uasOperatorId;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DroneFlight> getFlights() {
        return flights;
    }

    public void setFlights(List<DroneFlight> flights) {
        this.flights = flights;
    }

    public int getFlightCount() {
        return flights != null ? flights.size() : 0;
    }

    public Duration getTotalFlightDuration() {
        if (flights == null || flights.isEmpty()) {
            return Duration.ZERO;
        }
        Duration total = Duration.ZERO;
        for (DroneFlight flight : flights) {
            if (flight.getDuration() != null) {
                total = total.plus(flight.getDuration());
            }
        }
        return total;
    }

    public double getTotalAirDistance() {
        if (flights == null || flights.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (DroneFlight flight : flights) {
            if (flight.getAirDistance() != null) {
                total += flight.getAirDistance();
            }
        }
        return total;
    }

    public String getTotalFlightHoursFormatted() {
        Duration d = getTotalFlightDuration();
        if (d == null || d.isZero()) return "0.0 h";
        double hours = d.toMinutes() / 60.0;
        return String.format(java.util.Locale.US, "%.1f h", hours);
    }
}
