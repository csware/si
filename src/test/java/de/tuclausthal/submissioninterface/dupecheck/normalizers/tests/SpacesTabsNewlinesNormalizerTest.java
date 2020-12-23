/*
 * Copyright 2012, 2020 Sven Strickroth <email@cs-ware.de>
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
package de.tuclausthal.submissioninterface.dupecheck.normalizers.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.SpacesTabsNewlinesNormalizer;

public class SpacesTabsNewlinesNormalizerTest {
	SpacesTabsNewlinesNormalizer b = new SpacesTabsNewlinesNormalizer();

	@Test
	public void testNormalizeEmpty() {
		StringBuffer stringBuffer = new StringBuffer("");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyTab() {
		StringBuffer stringBuffer = new StringBuffer("\t");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlySpace() {
		StringBuffer stringBuffer = new StringBuffer(" ");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyNewline() {
		StringBuffer stringBuffer = new StringBuffer("\n");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyCarriageReturn() {
		StringBuffer stringBuffer = new StringBuffer("\r");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyCRLF() {
		StringBuffer stringBuffer = new StringBuffer("\r\n");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeCRLF() {
		StringBuffer stringBuffer = new StringBuffer("Some\r\nthing");
		assertEquals("Some thing", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeSomeCode() {
		StringBuffer stringBuffer = new StringBuffer("public Test  {\n\n\tprivate int i\t=\t1;\r\nprivate int o\t= 2;\n}\t \n");
		assertEquals("public Test { private int i = 1; private int o = 2; }", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyTwoNewlines() {
		StringBuffer stringBuffer = new StringBuffer("\n\n");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyTwoSpaces() {
		StringBuffer stringBuffer = new StringBuffer("  ");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndsWithNewlines() {
		StringBuffer stringBuffer = new StringBuffer("something\n\n");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeStartsWithNewlines() {
		StringBuffer stringBuffer = new StringBuffer("\n \nsomething");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}
}
