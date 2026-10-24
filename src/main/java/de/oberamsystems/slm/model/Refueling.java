package de.oberamsystems.slm.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.UUID;

import de.oberamsystems.slm.config.TimeZoneConfig;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refueling")
public class Refueling {

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_id", nullable = false)
	private Car car;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "station_id")
	private Station station;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "charging_point_id")
	private ChargingPoint chargingPoint;

	@Column(name = "fueling_date", nullable = false, columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime fuelingDate;

	@Column(name = "odometer", precision = 10, scale = 2)
	private BigDecimal odometer;

	@Column(name = "trip_distance", precision = 10, scale = 2)
	private BigDecimal tripDistance;

	@Column(name = "liters", precision = 8, scale = 2, nullable = false)
	private BigDecimal liters;

	@Column(name = "cost_per_liter", precision = 8, scale = 3)
	private BigDecimal costPerLiter;

	@Column(name = "total_cost", precision = 8, scale = 2, nullable = false)
	private BigDecimal totalCost;

	@Column(name = "currency", length = 3, nullable = false)
	private String currency = "EUR";

	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	@Column(name = "fuel_type")
	private FuelType fuelType;

	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	@Column(name = "fueling_type")
	private FuelingType fuelingType = FuelingType.FULL;

	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	@Column(name = "driving_style")
	private DrivingStyle drivingStyle;

	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	@Column(name = "tire_type")
	private TireType tireType;

	@Column(name = "country", length = 2)
	private String country;

	@Column(name = "location", columnDefinition = "TEXT")
	private String location;

	@Column(name = "routes", columnDefinition = "TEXT")
	private String routes;

	@Column(name = "calculated_consumption", precision = 6, scale = 2)
	private BigDecimal calculatedConsumption;

	/** BC (Bordcomputer / Board Computer) estimated fuel consumption in L/100km */
	@Column(name = "board_computer_consumption", precision = 6, scale = 2)
	private BigDecimal boardComputerConsumption;

	/** BC (Bordcomputer / Board Computer) estimated consumed fuel volume in Liters */
	@Column(name = "board_computer_quantity", precision = 8, scale = 2)
	private BigDecimal boardComputerQuantity;

	/** BC (Bordcomputer / Board Computer) average trip speed in km/h */
	@Column(name = "board_computer_speed", precision = 6, scale = 2)
	private BigDecimal boardComputerSpeed;

	@Column(name = "note", columnDefinition = "TEXT")
	private String note;

	@Column(name = "created_at", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime createdAt = OffsetDateTime.now(TimeZoneConfig.getZoneId());

	public Refueling() {
	}

	public void autoCalculateMetrics() {
		if (costPerLiter == null && totalCost != null && liters != null && liters.compareTo(BigDecimal.ZERO) > 0) {
			this.costPerLiter = totalCost.divide(liters, 3, RoundingMode.HALF_UP);
		} else if (totalCost == null && costPerLiter != null && liters != null) {
			this.totalCost = costPerLiter.multiply(liters).setScale(2, RoundingMode.HALF_UP);
		}
		if (calculatedConsumption == null && liters != null && tripDistance != null && tripDistance.compareTo(BigDecimal.ZERO) > 0) {
			this.calculatedConsumption = liters.multiply(BigDecimal.valueOf(100)).divide(tripDistance, 2, RoundingMode.HALF_UP);
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Station getStation() {
		return station;
	}

	public void setStation(Station station) {
		this.station = station;
	}

	public ChargingPoint getChargingPoint() {
		return chargingPoint;
	}

	public void setChargingPoint(ChargingPoint chargingPoint) {
		this.chargingPoint = chargingPoint;
	}

	public OffsetDateTime getFuelingDate() {
		return fuelingDate;
	}

	public void setFuelingDate(OffsetDateTime fuelingDate) {
		this.fuelingDate = fuelingDate;
	}

	public BigDecimal getOdometer() {
		return odometer;
	}

	public void setOdometer(BigDecimal odometer) {
		this.odometer = odometer;
	}

	public BigDecimal getTripDistance() {
		return tripDistance;
	}

	public void setTripDistance(BigDecimal tripDistance) {
		this.tripDistance = tripDistance;
	}

	public BigDecimal getLiters() {
		return liters;
	}

	public void setLiters(BigDecimal liters) {
		this.liters = liters;
	}

	public BigDecimal getCostPerLiter() {
		return costPerLiter;
	}

	public void setCostPerLiter(BigDecimal costPerLiter) {
		this.costPerLiter = costPerLiter;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		if (currency != null) {
			currency = currency.trim().toUpperCase();
			Currency.getInstance(currency); // validates ISO 4217
		}
		this.currency = currency != null ? currency : "EUR";
	}

	public FuelType getFuelType() {
		return fuelType;
	}

	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	public FuelingType getFuelingType() {
		return fuelingType;
	}

	public void setFuelingType(FuelingType fuelingType) {
		this.fuelingType = fuelingType != null ? fuelingType : FuelingType.FULL;
	}

	public DrivingStyle getDrivingStyle() {
		return drivingStyle;
	}

	public void setDrivingStyle(DrivingStyle drivingStyle) {
		this.drivingStyle = drivingStyle;
	}

	public TireType getTireType() {
		return tireType;
	}

	public void setTireType(TireType tireType) {
		this.tireType = tireType;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		if (country != null) {
			country = country.trim().toUpperCase();
			if (!country.matches("^[A-Z]{2}$")) {
				throw new IllegalArgumentException("Country must be a 2-letter ISO 3166 code: " + country);
			}
		}
		this.country = country;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRoutes() {
		return routes;
	}

	public void setRoutes(String routes) {
		this.routes = routes;
	}

	public BigDecimal getCalculatedConsumption() {
		return calculatedConsumption;
	}

	public void setCalculatedConsumption(BigDecimal calculatedConsumption) {
		this.calculatedConsumption = calculatedConsumption;
	}

	public BigDecimal getBoardComputerConsumption() {
		return boardComputerConsumption;
	}

	public void setBoardComputerConsumption(BigDecimal boardComputerConsumption) {
		this.boardComputerConsumption = boardComputerConsumption;
	}

	public BigDecimal getBoardComputerQuantity() {
		return boardComputerQuantity;
	}

	public void setBoardComputerQuantity(BigDecimal boardComputerQuantity) {
		this.boardComputerQuantity = boardComputerQuantity;
	}

	public BigDecimal getBoardComputerSpeed() {
		return boardComputerSpeed;
	}

	public void setBoardComputerSpeed(BigDecimal boardComputerSpeed) {
		this.boardComputerSpeed = boardComputerSpeed;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
