package de.tuclausthal.abgabesystem.dupecheck;

public class CapitalizationNormalizer implements Normalizer {

	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		int i = 0;
		while (i < stringBuffer.length()) {
			stringBuffer.setCharAt(i, Character.toLowerCase(stringBuffer.charAt(i)));
			i++;
		}
		return stringBuffer;
	}
}
