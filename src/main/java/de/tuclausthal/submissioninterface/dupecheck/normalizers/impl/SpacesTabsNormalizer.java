/*
 * Copyright 2009, 2012, 2021 Sven Strickroth <email@cs-ware.de>
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
 * Normalizer to normalze tabs/spaces to one space
 * @author Sven Strickroth
 */
public class SpacesTabsNormalizer implements NormalizerIf {

	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		int i = 0;
		while (i < stringBuffer.length()) {
			if (stringBuffer.charAt(i) == '\t' || stringBuffer.charAt(i) == ' ') {
				if (i != 0) {
					stringBuffer.setCharAt(i, ' ');
					++i;
				}
				int j = i;
				while (j < stringBuffer.length()) {
					if (!(stringBuffer.charAt(j) == '\t' || stringBuffer.charAt(j) == ' ')) {
						break;
					}
					++j;
				}
				stringBuffer.delete(i, j);
			}
			++i;
		}
		i = stringBuffer.length() - 1; 
		if (i >= 0 && stringBuffer.charAt(i) == ' ') {
			stringBuffer.setLength(i);
		}
		return stringBuffer;
	}
}
