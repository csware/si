/*
 * Copyright 2012 Sven Strickroth <email@cs-ware.de>
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

import static org.junit.Assert.*;

import org.junit.Test;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCodeNormalizer;

public class StripCodeNormalizerTest {
	StripCodeNormalizer b = new StripCodeNormalizer();

	@Test
	public void testNormalizeStartWithLineComment() {
		StringBuffer stringBuffer = new StringBuffer("//something");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEmpty() {
		StringBuffer stringBuffer = new StringBuffer("");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyWithLineComment() {
		StringBuffer stringBuffer = new StringBuffer("//");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentInLineComment() {
		StringBuffer stringBuffer = new StringBuffer("// some // thing");
		assertEquals(" some // thing", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultiLineCommentInMultiLineComment() {
		StringBuffer stringBuffer = new StringBuffer("/* some /* thing */ else*/");
		assertEquals(" some /* thing ", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyOneChar() {
		StringBuffer stringBuffer = new StringBuffer("/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeThreeChars() {
		StringBuffer stringBuffer = new StringBuffer("///");
		assertEquals("/", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeTypoComment() {
		StringBuffer stringBuffer = new StringBuffer("//* */");
		assertEquals("* */", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeTypoComment2() {
		StringBuffer stringBuffer = new StringBuffer("/ * */");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyWithLineCommentAndNewLine() {
		StringBuffer stringBuffer = new StringBuffer("//\n");
		assertEquals("\n", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/*something*/");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineCommentAndPostfix() {
		StringBuffer stringBuffer = new StringBuffer("/*something*/thingelse");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineCommentInLineComment() {
		StringBuffer stringBuffer = new StringBuffer("///*something*/thingelse");
		assertEquals("/*something*/thingelse", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeUnClosedMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/*something");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentInMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/*some//thing*/");
		assertEquals("some//thing", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMixed() {
		StringBuffer stringBuffer = new StringBuffer("something //else\neven\n/*\nmore\n*/stuff");
		assertEquals("else\nmore\n", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineCommentAndPrefix() {
		StringBuffer stringBuffer = new StringBuffer("thingelse/*something*/");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyWithMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/**/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndWithLineComment() {
		StringBuffer stringBuffer = new StringBuffer("something//");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndWithMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("something/**/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeStartWithMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/**/something");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

}
