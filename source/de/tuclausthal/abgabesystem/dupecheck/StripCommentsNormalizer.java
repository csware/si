package de.tuclausthal.abgabesystem.dupecheck;

public class StripCommentsNormalizer implements Normalizer {
	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		int i = 0;
		while (i < stringBuffer.length() - 1) {
			if ("//".equals(stringBuffer.substring(i, i + 2))) {
				while (i < stringBuffer.length() - 1) {
					if (stringBuffer.charAt(i) != '\n') {
						stringBuffer.deleteCharAt(i);
					} else {
						break;
					}
				}
			}
			if ("/*".equals(stringBuffer.substring(i, i + 2))) {
				while (i < stringBuffer.length() - 1) {
					if ("*/".equals(stringBuffer.substring(i, i + 2))) {
						stringBuffer.deleteCharAt(i);
						stringBuffer.deleteCharAt(i);
						i--;
						break;
					} else {
						stringBuffer.deleteCharAt(i);
					}
				}
			}
			i++;
		}
		return stringBuffer;
	}
}
