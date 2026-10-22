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

	public record ChargingPointGroup(PlugType plugType, BigDecimal maxPowerKw, int count) {
		public String getDisplayName() {
			String name = (plugType != null) ? plugType.getDisplayName() : "Unknown";
			return count + "x " + name + " (" + maxPowerKw + " kW)";
		}
	}

	public List<ChargingPointGroup> getChargingPointGroups() {
		if (chargingPoints == null || chargingPoints.isEmpty()) {
			return List.of();
		}
		java.util.Map<String, ChargingPointGroup> map = new java.util.LinkedHashMap<>();
		for (ChargingPoint cp : chargingPoints) {
			if (cp.getPlugType() == null || cp.getMaxPowerKw() == null) {
				continue;
			}
			String key = cp.getPlugType().name() + "_" + cp.getMaxPowerKw().stripTrailingZeros().toPlainString();
			ChargingPointGroup existing = map.get(key);
			if (existing == null) {
				map.put(key, new ChargingPointGroup(cp.getPlugType(), cp.getMaxPowerKw(), 1));
			} else {
				map.put(key, new ChargingPointGroup(cp.getPlugType(), cp.getMaxPowerKw(), existing.count() + 1));
			}
		}
		List<ChargingPointGroup> list = new ArrayList<>(map.values());
		list.sort((a, b) -> b.maxPowerKw().compareTo(a.maxPowerKw()));
		return list;
	}

	public String getChargingPointGroupsJson() {
		List<ChargingPointGroup> groups = getChargingPointGroups();
		if (groups.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < groups.size(); i++) {
			ChargingPointGroup g = groups.get(i);
			if (i > 0) sb.append(",");
			sb.append("{\"plugType\":\"").append(g.plugType().name())
			  .append("\",\"maxPowerKw\":").append(g.maxPowerKw())
			  .append(",\"count\":").append(g.count()).append("}");
		}
		sb.append("]");
		return sb.toString();
	}
}
