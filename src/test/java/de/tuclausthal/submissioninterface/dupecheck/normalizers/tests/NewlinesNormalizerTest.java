/*
 * Copyright 2012, 2020 Sven Strickroth <email@cs-ware.de>
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
package de.tuclausthal.submissioninterface.dupecheck.normalizers.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.NewlinesNormalizer;

public class NewlinesNormalizerTest {
	NewlinesNormalizer b = new NewlinesNormalizer();

	@Test
	public void testNormalizeEmpty() {
		StringBuffer stringBuffer = new StringBuffer("");
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
		assertEquals("Some\nthing", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyTwoNewlines() {
		StringBuffer stringBuffer = new StringBuffer("\n\n");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndsWithNewlines() {
		StringBuffer stringBuffer = new StringBuffer("something\n\n");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeStartsWithNewlines() {
		StringBuffer stringBuffer = new StringBuffer("\n\nsomething");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}
}
