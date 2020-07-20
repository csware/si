/*
 * Copyright 2009, 2012, 2015, 2017 Sven Strickroth <email@cs-ware.de>
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
 * Normalizer to drop the comments from the sourcecode 
 * @author Sven Strickroth
 */
public class StripCommentsNormalizer implements NormalizerIf {
	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		int i = 0;
		while (i < stringBuffer.length() - 1) {
			if (stringBuffer.charAt(i) == '"') {
				++i;
				while (i < stringBuffer.length()) {
					if (stringBuffer.charAt(i) == '\\') {
						i += 2;
						continue;
					}
					if (stringBuffer.charAt(i) == '"' || stringBuffer.charAt(i) == '\n') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (stringBuffer.charAt(i) == '\'') {
				++i;
				while (i < stringBuffer.length()) {
					if (stringBuffer.charAt(i) == '\\') {
						i += 2;
						continue;
					}
					if (stringBuffer.charAt(i) == '\'' || stringBuffer.charAt(i) == '\n') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if ("//".equals(stringBuffer.substring(i, i + 2))) {
				while (i < stringBuffer.length()) {
					if (stringBuffer.charAt(i) != '\n') {
						stringBuffer.deleteCharAt(i);
					} else {
						break;
					}
				}
			}
			if (i < stringBuffer.length() - 1 && "/*".equals(stringBuffer.substring(i, i + 2))) {
				stringBuffer.deleteCharAt(i);
				stringBuffer.deleteCharAt(i);
				while (i < stringBuffer.length()) {
					if (i < stringBuffer.length() - 1 && "*/".equals(stringBuffer.substring(i, i + 2))) {
						stringBuffer.deleteCharAt(i);
						stringBuffer.deleteCharAt(i);
						i--;
						break;
					}
					stringBuffer.deleteCharAt(i);
				}
			}
			i++;
		}
		return stringBuffer;
	}
}
