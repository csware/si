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

package de.tuclausthal.submissioninterface.dupecheck.normalizers.impl;

import java.util.Stack;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerIf;

/**
 * Normalizer stack to stack normalizers/define a list to run one after another
 * @author Sven Strickroth
 */
public class NormalizerStack implements NormalizerIf {
	private Stack<NormalizerIf> normalizersStack = new Stack<>();

	/**
	 * Adds a normalizer to the stack (on the top)
	 * @param normalizer
	 */
	public void addNormalizer(NormalizerIf normalizer) {
		normalizersStack.push(normalizer);
	}

	@Override
	public StringBuffer normalize(StringBuffer stringBuffer) {
		for (NormalizerIf normlizer : normalizersStack) {
			stringBuffer = normlizer.normalize(stringBuffer);
		}
		return stringBuffer;
	}

}
