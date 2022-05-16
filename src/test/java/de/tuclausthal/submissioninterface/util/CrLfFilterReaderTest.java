/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

public class CrLfFilterReaderTest {
	static String normalizeThroughFilter(String input) {
		Reader sr = new CrLfFilterReader(new StringReader(input));
		StringWriter sw = new StringWriter(input.length());
		try {
			IOUtils.copy(sr, sw);
		} catch (IOException e) {
			fail(e);
		}
		return sw.toString();
	}

	@Test
	public void testCrLfEmpty() {
		assertEquals("", normalizeThroughFilter(""));
	}

	@Test
	public void testCrLfJustLf() {
		assertEquals("\n\n\n", normalizeThroughFilter("\n\n\n"));
	}

	@Test
	public void testCrLfSingleCr() {
		assertEquals("\r", normalizeThroughFilter("\r"));
	}

	@Test
	public void testCrLfSingleCrChar() {
		assertEquals("\rb", normalizeThroughFilter("\rb"));
	}

	@Test
	public void testCrLfSingleCrLf() {
		assertEquals("\n", normalizeThroughFilter("\r\n"));
	}

	@Test
	public void testCrLfSingleChar() {
		assertEquals("a", normalizeThroughFilter("a"));
	}

	@Test
	public void testCrLfJustCr() {
		assertEquals("\r\r\r", normalizeThroughFilter("\r\r\r"));
	}

	@Test
	public void testCrLfMixed() {
		assertEquals("\n\r\n\n\r", normalizeThroughFilter("\r\n\r\r\n\n\r"));
	}

	@Test
	public void testCrLfMulti() {
		assertEquals("\n\n\n", normalizeThroughFilter("\r\n\r\n\r\n"));
	}

	@Test
	public void testCrLfNormal() {
		assertEquals("\n \nfdfdsf\n ", normalizeThroughFilter("\r\n \r\nfdfdsf\r\n "));
	}
}
