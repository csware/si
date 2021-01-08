/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

public class UtilTest {
	@Test
	public void testCleanCrLfEmpty() {
		StringBuffer stringBuffer = new StringBuffer("");
		Util.cleanCrLf(stringBuffer);
		assertEquals("", stringBuffer.toString());
	}

	@Test
	public void testCleanCrLfJustLf() {
		StringBuffer stringBuffer = new StringBuffer("\n\n\n");
		Util.cleanCrLf(stringBuffer);
		assertEquals("\n\n\n", stringBuffer.toString());
	}

	@Test
	public void testCleanCrLfJustCr() {
		StringBuffer stringBuffer = new StringBuffer("\r\r\r");
		Util.cleanCrLf(stringBuffer);
		assertEquals("\r\r\r", stringBuffer.toString());
	}

	@Test
	public void testCleanCrLfMixed() {
		StringBuffer stringBuffer = new StringBuffer("\r\n\r\r\n\n\r");
		Util.cleanCrLf(stringBuffer);
		assertEquals("\n\r\n\n\r", stringBuffer.toString());
	}

	@Test
	public void testCleanCrLfMulti() {
		StringBuffer stringBuffer = new StringBuffer("\r\n\r\n\r\n");
		Util.cleanCrLf(stringBuffer);
		assertEquals("\n\n\n", stringBuffer.toString());
	}

	@Test
	public void testCleanCrLfNormal() {
		StringBuffer stringBuffer = new StringBuffer("\r\n \r\nfdfdsf\r\n ");
		Util.cleanCrLf(stringBuffer);
		assertEquals("\n \nfdfdsf\n ", stringBuffer.toString());
	}

	@Test
	public void testGetCurrentSemester() {
		Util.CLOCK = Clock.fixed(Instant.parse("2020-12-21T12:00:00Z"), ZoneOffset.UTC);

		assertEquals(20201, Util.getCurrentSemester());

		Util.CLOCK = Clock.fixed(Instant.parse("2021-01-08T10:00:00Z"), ZoneOffset.UTC);

		assertEquals(20201, Util.getCurrentSemester());

		Util.CLOCK = Clock.fixed(Instant.parse("2021-03-16T10:00:00Z"), ZoneOffset.UTC);

		assertEquals(20210, Util.getCurrentSemester());

		Util.CLOCK = Clock.fixed(Instant.parse("2021-04-08T10:00:00Z"), ZoneOffset.UTC);

		assertEquals(20210, Util.getCurrentSemester());

		Util.CLOCK = Clock.fixed(Instant.parse("2021-07-08T10:00:00Z"), ZoneOffset.UTC);

		assertEquals(20210, Util.getCurrentSemester());

		Util.CLOCK = Clock.fixed(Instant.parse("2021-08-08T10:00:00Z"), ZoneOffset.UTC);

		assertEquals(20210, Util.getCurrentSemester());

		Util.CLOCK = Clock.fixed(Instant.parse("2021-09-08T10:00:00Z"), ZoneOffset.UTC);

		assertEquals(20211, Util.getCurrentSemester());

		Util.CLOCK = Clock.systemDefaultZone();
	}

	@Test
	public void testEscapeHTML() {
		assertEquals("", Util.escapeHTML(""));
		assertEquals("", Util.escapeHTML(null));
		assertEquals("some\nthing", Util.escapeHTML("some\nthing"));
		assertEquals("&amp;s&lt;b&gt;ometh&quot;ing", Util.escapeHTML("&s<b>ometh\"ing"));
	}

	@Test
	public void testMakeCleanHTML() {
		assertEquals("", Util.makeCleanHTML(""));
		assertEquals("", Util.makeCleanHTML(null));
		assertEquals("some\nthing", Util.makeCleanHTML("some\nthing"));
		assertEquals("&amp;s<b>ometh&quot;ing&lt;script&gt;<div style=\"fff\">", Util.makeCleanHTML("&s<b>ometh\"ing<script><div style=\"fff\">"));
	}

	@Test
	public void testTextToHTML() {
		assertEquals("", Util.textToHTML(""));
		assertEquals("", Util.textToHTML(null));
		assertEquals("some<br>thing", Util.textToHTML("some\nthing"));
		assertEquals("some\r<br>thing", Util.textToHTML("some\r\nthing"));
	}

	@Test
	public void testShowPoints() {
		assertEquals("0", Util.showPoints(0));
		assertEquals("1", Util.showPoints(100));
		assertEquals("1,5", Util.showPoints(150));
		assertEquals("1,05", Util.showPoints(105));
	}

	@Test
	public void testConvertToPoints() {
		assertEquals(0, Util.convertToPoints("0"));
		assertEquals(0, Util.convertToPoints("-5"));
		assertEquals(0, Util.convertToPoints("something"));
		assertEquals(100, Util.convertToPoints("1"));
		assertEquals(150, Util.convertToPoints("1,5"));
		assertEquals(170, Util.convertToPoints("1,7"));
		assertEquals(170, Util.convertToPoints("1.7"));
		assertEquals(175, Util.convertToPoints("1,75"));
		assertEquals(20000, Util.convertToPoints("200"));
	}

	@Test
	public void testConvertToPointsMinPointStep() {
		assertEquals(0, Util.convertToPoints("0", 50));
		assertEquals(0, Util.convertToPoints("-5", 50));
		assertEquals(0, Util.convertToPoints("something", 50));
		assertEquals(100, Util.convertToPoints("1", 50));
		assertEquals(50, Util.convertToPoints("0,5", 50));
		assertEquals(150, Util.convertToPoints("1,5", 50));
		assertEquals(150, Util.convertToPoints("1,7", 50));
		assertEquals(150, Util.convertToPoints("1.7", 50));
		assertEquals(150, Util.convertToPoints("1,75", 50));
	}

	@Test
	public void testLowerCaseExtension() {
		StringBuffer buffer;
		buffer = new StringBuffer();
		Util.lowerCaseExtension(buffer);
		assertEquals("", buffer.toString());

		buffer = new StringBuffer("bLa");
		Util.lowerCaseExtension(buffer);
		assertEquals("bLa", buffer.toString());

		buffer = new StringBuffer("Hello.JaVa");
		Util.lowerCaseExtension(buffer);
		assertEquals("Hello.java", buffer.toString());
	}
}
