/*
 * Copyright 2010-2012, 2017, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Sven Strickroth
 */
public class JavaFunctionTestTest {
	@Test
	public void testStartsWith() {
		assertEquals(-1, JavaFunctionTest.startsWith(new StringBuffer(), "abc"));
		assertEquals(-1, JavaFunctionTest.startsWith(new StringBuffer("abc"), "abcd"));

		assertEquals(3, JavaFunctionTest.startsWith(new StringBuffer("abc"), "abc"));

		assertEquals(7, JavaFunctionTest.startsWith(new StringBuffer("abc\ndef"), "abc\ndef"));
		assertEquals(8, JavaFunctionTest.startsWith(new StringBuffer("abc\r\ndef"), "abc\ndef"));

		assertEquals(7, JavaFunctionTest.startsWith(new StringBuffer("abc\ndefa"), "abc\ndef"));
		assertEquals(8, JavaFunctionTest.startsWith(new StringBuffer("abc\r\ndefa"), "abc\ndef"));

		assertEquals(8, JavaFunctionTest.startsWith(new StringBuffer("abc\ndef\n"), "abc\ndef\n"));
		assertEquals(10, JavaFunctionTest.startsWith(new StringBuffer("abc\r\ndef\r\n"), "abc\ndef\n"));

		assertEquals(8, JavaFunctionTest.startsWith(new StringBuffer("abc\r\n\r\nd"), "abc\n\nd"));

		assertEquals(8, JavaFunctionTest.startsWith(new StringBuffer("abc\ndef\n\ndffsdfdf"), "abc\ndef\n"));

		assertEquals(-1, JavaFunctionTest.startsWith(new StringBuffer("abc\r\r\nd"), "abc\nd"));
	}
	
	@Test
	public void testCleanupStdErr() {
		StringBuffer sb = new StringBuffer("WARNING: A command line option has enabled the Security Manager\nWARNING: The Security Manager is deprecated and will be removed in a future release\n");
		JavaFunctionTest.cleanupStdErr(sb);
		assertEquals(0, sb.length());

		sb = new StringBuffer("WARNING: A command line option has enabled the Security Manager\r\nWARNING: The Security Manager is deprecated and will be removed in a future release\r\nMore Stuff");
		JavaFunctionTest.cleanupStdErr(sb);
		assertEquals("More Stuff", sb.toString());
	}
}
