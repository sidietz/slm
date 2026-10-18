package de.oberamsystems.slm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.oberamsystems.slm.model.TrainLine;

public class TrainLineComparatorTest {

	@Test
	public void testNaturalOrdering() {
		List<TrainLine> lines = new ArrayList<>();
		lines.add(new TrainLine(0L, "S4", "S-Bahn 4", null));
		lines.add(new TrainLine(1L, "RE90", "Regionalexpress 90", null));
		lines.add(new TrainLine(2L, "RE28", "Regionalexpress 28", null));
		lines.add(new TrainLine(4L, "S1", "S-Bahn 1", null));
		lines.add(new TrainLine(5L, "RE1", "Regionalexpress 1", null));

		lines.sort(new TrainLineComparator());

		assertEquals("RE1", lines.get(0).getName());
		assertEquals("RE28", lines.get(1).getName());
		assertEquals("RE90", lines.get(2).getName());
		assertEquals("S1", lines.get(3).getName());
		assertEquals("S4", lines.get(4).getName());
	}
}
