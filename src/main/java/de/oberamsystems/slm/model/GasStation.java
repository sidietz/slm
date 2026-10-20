package de.oberamsystems.slm.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "gas_station")
@DiscriminatorValue("GAS_STATION")
public class GasStation extends Station {

	@Column(name = "tankentanken_id", columnDefinition = "uuid", unique = true)
	private UUID tankentankenId;

	public GasStation() {
		super();
		setStationType(StationType.GAS_STATION);
	}

	public UUID getTankentankenId() {
		return tankentankenId;
	}

	public void setTankentankenId(UUID tankentankenId) {
		this.tankentankenId = tankentankenId;
	}
}
