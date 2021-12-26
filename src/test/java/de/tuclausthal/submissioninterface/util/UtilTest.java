/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
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
}
