package de.oberamsystems.slm.util;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.oberamsystems.slm.model.TrainLine;

public class TrainLineComparator implements Comparator<TrainLine> {

	private static final Pattern PATTERN = Pattern.compile("^([A-Za-z]+)\\s*(\\d+)?(.*)$");

	@Override
	public int compare(TrainLine o1, TrainLine o2) {
		if (o1 == null && o2 == null) return 0;
		if (o1 == null || o1.getName() == null) return 1;
		if (o2 == null || o2.getName() == null) return -1;

		String s1 = o1.getName().trim();
		String s2 = o2.getName().trim();

		Matcher m1 = PATTERN.matcher(s1);
		Matcher m2 = PATTERN.matcher(s2);

		if (m1.matches() && m2.matches()) {
			String prefix1 = m1.group(1).toUpperCase();
			String prefix2 = m2.group(1).toUpperCase();
			int prefixCmp = prefix1.compareTo(prefix2);
			if (prefixCmp != 0) {
				return prefixCmp;
			}

			String numStr1 = m1.group(2);
			String numStr2 = m2.group(2);
			if (numStr1 != null && numStr2 != null) {
				try {
					long n1 = Long.parseLong(numStr1);
					long n2 = Long.parseLong(numStr2);
					int numCmp = Long.compare(n1, n2);
					if (numCmp != 0) {
						return numCmp;
					}
				} catch (NumberFormatException ignored) {
				}
			} else if (numStr1 != null) {
				return -1;
			} else if (numStr2 != null) {
				return 1;
			}
		}

		return s1.compareToIgnoreCase(s2);
	}
}
