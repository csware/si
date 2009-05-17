package de.tuclausthal.abgabesystem.dupecheck;

public class NewlinesNormalizer implements Normalizer {

	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		int i = 0;
		boolean lastWasNewline = true;
		while (i < stringBuffer.length()) {
			if (stringBuffer.charAt(i) == '\r' || stringBuffer.charAt(i) == '\n') {
				stringBuffer.setCharAt(i, '\n');
				if (lastWasNewline) {
					stringBuffer.deleteCharAt(i);
				} else {
					i++;
				}
				lastWasNewline = true;
			} else {
				lastWasNewline = false;
				i++;
			}
		}
		return stringBuffer;
	}
}
