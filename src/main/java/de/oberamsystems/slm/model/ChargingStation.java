package de.oberamsystems.slm.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "charging_station")
@DiscriminatorValue("CHARGING_STATION")
public class ChargingStation extends Station {

	@Column(name = "operator", columnDefinition = "TEXT")
	private String operator;

	@OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChargingPoint> chargingPoints = new ArrayList<>();

	public ChargingStation() {
		super();
		setStationType(StationType.CHARGING_STATION);
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public List<ChargingPoint> getChargingPoints() {
		return chargingPoints;
	}

	public void setChargingPoints(List<ChargingPoint> chargingPoints) {
		this.chargingPoints = chargingPoints;
	}

	public void addChargingPoint(ChargingPoint point) {
		if (point != null) {
			point.setChargingStation(this);
			this.chargingPoints.add(point);
		}
	}

	public BigDecimal getMaxPowerKw() {
		if (chargingPoints == null || chargingPoints.isEmpty()) {
			return BigDecimal.ZERO;
		}
		return chargingPoints.stream()
				.map(ChargingPoint::getMaxPowerKw)
				.filter(Objects::nonNull)
				.max(BigDecimal::compareTo)
				.orElse(BigDecimal.ZERO);
	}

	public int getNumberOfChargingPoints() {
		return chargingPoints != null ? chargingPoints.size() : 0;
	}
}
