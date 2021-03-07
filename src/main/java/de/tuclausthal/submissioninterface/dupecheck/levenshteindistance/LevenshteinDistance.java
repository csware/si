/*
 * Copyright 2009, 2017 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dupecheck.levenshteindistance;

import java.io.File;
import java.io.IOException;

import de.tuclausthal.submissioninterface.dupecheck.DupeCheck;

/**
 * Levenshtein-distance plagiarism test implementation
 * @author Sven Strickroth
 */
public class LevenshteinDistance extends DupeCheck {
	public LevenshteinDistance(File path) {
		super(path);
	}

	@Override
	protected int calculateSimilarity(StringBuffer fileOne, StringBuffer fileTwo, int maximumDifferenceInPercent) throws IOException {
		int maxLength = Math.max(fileOne.length(), fileTwo.length());
		if (Math.abs((double) Math.abs(fileOne.length() - fileTwo.length())) / maxLength * 100.0 <= maximumDifferenceInPercent) {
			double distance = (1 - (double) Levenshtein.getLevenshteinDistance(fileOne, fileTwo, (int) (maximumDifferenceInPercent / 100.0 * maxLength)) / maxLength) * 100.0;
			return (int) distance;
		}
		return 0;
	}
}
