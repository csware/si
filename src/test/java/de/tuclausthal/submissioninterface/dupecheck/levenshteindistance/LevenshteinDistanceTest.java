/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class LevenshteinDistanceTest {
	@Test
	public void testFull() throws IOException {
		LevenshteinDistance distance = new LevenshteinDistance(null);
		assertEquals(100, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer("12345678901234567890"), 100));
		assertEquals(80, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer("1234567890123456"), 100));
		assertEquals(0, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer(""), 100));
	}

	@Test
	public void testThreshhold() throws IOException {
		LevenshteinDistance distance = new LevenshteinDistance(null);
		assertEquals(100, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer("12345678901234567890"), 20));
		assertEquals(90, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer("123456789012345678"), 20));
		assertEquals(80, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer("1234567890123456"), 20));
		assertEquals(0, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer("123456789012345"), 20));
		assertEquals(0, distance.calculateSimilarity(new StringBuffer("12345678901234567890"), new StringBuffer(""), 20));
	}
}
