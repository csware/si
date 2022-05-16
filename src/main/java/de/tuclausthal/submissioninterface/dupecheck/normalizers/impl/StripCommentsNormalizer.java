/*
 * Copyright 2009, 2012, 2015, 2017, 2021 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
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
				int j = i;
				while (j < stringBuffer.length()) {
					if (stringBuffer.charAt(j) == '\n') {
						break;
					}
					++j;
				}
				stringBuffer.delete(i, j);
			}
			if (i < stringBuffer.length() - 1 && "/*".equals(stringBuffer.substring(i, i + 2))) {
				int start = i;
				int j = i + 2;
				while (j < stringBuffer.length()) {
					if (j < stringBuffer.length() - 1 && "*/".equals(stringBuffer.substring(j, j + 2))) {
						j += 2;
						--i;
						break;
					}
					++j;
				}
				stringBuffer.delete(start, j);
			}
			i++;
		}
		return stringBuffer;
	}
}
