/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.dupecheck.normalizers.impl;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerIf;

/**
 * Normalizer to change all chars to lowercase
 * @author Sven Strickroth
 */
public class CapitalizationNormalizer implements NormalizerIf {

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
