package de.oberamsystems.slm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
@Table(name = "charging_point")
public class ChargingPoint {

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "charging_station_id", nullable = false)
	private ChargingStation chargingStation;

	@Column(name = "point_identifier", columnDefinition = "TEXT")
	private String pointIdentifier;

	@Column(name = "max_power_kw", precision = 6, scale = 1, nullable = false)
	private BigDecimal maxPowerKw;

	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	@Column(name = "plug_type", nullable = false)
	private PlugType plugType;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	public ChargingPoint() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public ChargingStation getChargingStation() {
		return chargingStation;
	}

	public void setChargingStation(ChargingStation chargingStation) {
		this.chargingStation = chargingStation;
	}

	public String getPointIdentifier() {
		return pointIdentifier;
	}

	public void setPointIdentifier(String pointIdentifier) {
		this.pointIdentifier = pointIdentifier;
	}

	public BigDecimal getMaxPowerKw() {
		return maxPowerKw;
	}

	public void setMaxPowerKw(BigDecimal maxPowerKw) {
		this.maxPowerKw = maxPowerKw;
	}

	public PlugType getPlugType() {
		return plugType;
	}

	public void setPlugType(PlugType plugType) {
		this.plugType = plugType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
