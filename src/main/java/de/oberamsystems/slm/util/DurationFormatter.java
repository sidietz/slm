package de.oberamsystems.slm.util;

import java.time.Duration;

public final class DurationFormatter {

	private DurationFormatter() {
	}

	public static String format(Duration duration) {
		if (duration == null) {
			return "0m";
		}
		long totalSeconds = Math.abs(duration.getSeconds());
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;

		if (hours > 0) {
			return String.format("%dh %02dm", hours, minutes);
		} else {
			return String.format("%dm", minutes);
		}
	}

	public static Duration fromMinutes(Long minutes) {
		if (minutes == null || minutes <= 0) {
			return Duration.ZERO;
		}
		return Duration.ofMinutes(minutes);
	}
}
