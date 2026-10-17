package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gaming_session")
@DiscriminatorValue("GAMING")
public class GamingSession extends Session {

	@ManyToOne
	@JoinColumn(name = "game_id", referencedColumnName = "id")
	private Game game;

	@ManyToOne
	@JoinColumn(name = "device_id", referencedColumnName = "id")
	private Device device;

	@Column(name = "game_mode", columnDefinition = "TEXT")
	private String gameMode;

	public GamingSession() {
		super();
	}

	public GamingSession(Game game, Device device, String gameMode) {
		super();
		this.game = game;
		this.device = device;
		this.gameMode = gameMode;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getGameMode() {
		return gameMode;
	}

	public void setGameMode(String gameMode) {
		this.gameMode = gameMode;
	}

	@Override
	public String toString() {
		return String.format("GamingSession [id=%s, game=%s, device=%s, gameMode=%s, session=%s]",
				getId(), game, device, gameMode, super.toString());
	}
}
