package de.tuclausthal.abgabesystem.dupecheck;

public class StackNormalizer implements Normalizer {
	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		stringBuffer = new StripCodeNormalizer().normalize(stringBuffer);
		stringBuffer = new SpacesTabsNewlinesNormalizer().normalize(stringBuffer);
		stringBuffer = new CapitalizationNormalizer().normalize(stringBuffer);
		return stringBuffer;
	}

}
