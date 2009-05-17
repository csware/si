package de.tuclausthal.abgabesystem.dupecheck;

public class SpacesTabsNormalizer implements Normalizer {

	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		int i = 0;
		boolean lastWasSpace = true;
		while (i < stringBuffer.length()) {
			if (stringBuffer.charAt(i) == '\t' || stringBuffer.charAt(i) == ' ') {
				stringBuffer.setCharAt(i, ' ');
				if (lastWasSpace) {
					stringBuffer.deleteCharAt(i);
				} else {
					i++;
				}
				lastWasSpace = true;
			} else {
				lastWasSpace = false;
				i++;
			}
		}
		return stringBuffer;
	}
}
