package de.tuclausthal.abgabesystem.dupecheck;

public class StripCodeNormalizer implements Normalizer {
	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		int i = 0;
		while (i < stringBuffer.length() - 1) {
			if ("//".equals(stringBuffer.substring(i, i + 2))) {
				stringBuffer.deleteCharAt(i);
				stringBuffer.deleteCharAt(i);
				while (i < stringBuffer.length() - 1) {
					if (stringBuffer.charAt(i) != '\n') {
						i++;
					} else {
						break;
					}
				}
			} else if ("/*".equals(stringBuffer.substring(i, i + 2))) {
				stringBuffer.deleteCharAt(i);
				stringBuffer.deleteCharAt(i);
				while (i < stringBuffer.length() - 1) {
					if ("*/".equals(stringBuffer.substring(i, i + 2))) {
						stringBuffer.deleteCharAt(i);
						stringBuffer.deleteCharAt(i);
						break;
					} else {
						i++;
					}
				}
			} else {
				stringBuffer.deleteCharAt(i);
			}
		}
		return stringBuffer;
	}
}
