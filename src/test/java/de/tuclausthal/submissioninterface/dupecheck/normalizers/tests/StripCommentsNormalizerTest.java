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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;

public class StripCommentsNormalizerTest {
	StripCommentsNormalizer b = new StripCommentsNormalizer();

	@Test
	public void testNormalizeStartWithLineComment() {
		StringBuffer stringBuffer = new StringBuffer("//something");
		assertEquals("", b.normalize(stringBuffer).toString());
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
	public void testNormalizeOnlyWithLineCommentAndNewLine() {
		StringBuffer stringBuffer = new StringBuffer("//\n");
		assertEquals("\n", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentInLineComment() {
		StringBuffer stringBuffer = new StringBuffer("// some // thing");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentInLineComment2() {
		StringBuffer stringBuffer = new StringBuffer("more // some // thing\nelse");
		assertEquals("more \nelse", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultiLineCommentInMultiLineComment() {
		StringBuffer stringBuffer = new StringBuffer("/* some /* thing */ else*/");
		assertEquals(" else*/", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyOneChar() {
		StringBuffer stringBuffer = new StringBuffer("/");
		assertEquals("/", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeThreeChars() {
		StringBuffer stringBuffer = new StringBuffer("///");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeTypoComment() {
		StringBuffer stringBuffer = new StringBuffer("//* */");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeTypoComment2() {
		StringBuffer stringBuffer = new StringBuffer("/ * */");
		assertEquals("/ * */", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyWithNewlineAndLineComment() {
		StringBuffer stringBuffer = new StringBuffer("\n//");
		assertEquals("\n", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/*something*/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineCommentAndPostfix() {
		StringBuffer stringBuffer = new StringBuffer("/*something*/thingelse");
		assertEquals("thingelse", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineCommentInLineComment() {
		StringBuffer stringBuffer = new StringBuffer("///*something*/thingelse");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeUnClosedMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/*something");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentInMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/*some//thing*/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMixed() {
		StringBuffer stringBuffer = new StringBuffer("something //else\neven\n/*\nmore\n*/stuff");
		assertEquals("something \neven\nstuff", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineCommentAndPrefix() {
		StringBuffer stringBuffer = new StringBuffer("thingelse/*something*/");
		assertEquals("thingelse", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeOnlyWithMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/**/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeFunnyLookingMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/*/something*/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndWithLineComment() {
		StringBuffer stringBuffer = new StringBuffer("something//");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndWithLineCommentAndNewline() {
		StringBuffer stringBuffer = new StringBuffer("something//\n");
		assertEquals("something\n", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndWithMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("something/**/");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeEndWithMultilineCommentAndNewline() {
		StringBuffer stringBuffer = new StringBuffer("something/**/\n");
		assertEquals("something\n", b.normalize(stringBuffer).toString());
	}
	
	@Test
	public void testNormalizeStartWithMultilineComment() {
		StringBuffer stringBuffer = new StringBuffer("/**/something");
		assertEquals("something", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeStartWithMultilineCommentAndNewline() {
		StringBuffer stringBuffer = new StringBuffer("/**/\nsomething");
		assertEquals("\nsomething", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuote() {
		StringBuffer stringBuffer = new StringBuffer("\"");
		assertEquals("\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuoteAtEnd() {
		StringBuffer stringBuffer = new StringBuffer("some\"");
		assertEquals("some\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenQuoteEscapedQuote() {
		StringBuffer stringBuffer = new StringBuffer("\"\\\"");
		assertEquals("\"\\\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuoteBackslash() {
		StringBuffer stringBuffer = new StringBuffer("\"\\\\\"");
		assertEquals("\"\\\\\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenQuote() {
		StringBuffer stringBuffer = new StringBuffer("\"some\nthing");
		assertEquals("\"some\nthing", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenQuoteSingleBackslash() {
		StringBuffer stringBuffer = new StringBuffer("\"some\\");
		assertEquals("\"some\\", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenQuoteBackslash() {
		StringBuffer stringBuffer = new StringBuffer("\"some\\\\");
		assertEquals("\"some\\\\", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenQuoteNextLineComment() {
		StringBuffer stringBuffer = new StringBuffer("\"some\n//thing");
		assertEquals("\"some\n", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuotes() {
		StringBuffer stringBuffer = new StringBuffer("\"\"");
		assertEquals("\"\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuotesWithLineComment() {
		StringBuffer stringBuffer = new StringBuffer("\"\"//some\nthing");
		assertEquals("\"\"\nthing", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuoteInQuotes() {
		StringBuffer stringBuffer = new StringBuffer("\"\\\"\"");
		assertEquals("\"\\\"\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentInQuotes() {
		StringBuffer stringBuffer = new StringBuffer("\"//something\"");
		assertEquals("\"//something\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultiLineCommentInQuotes() {
		StringBuffer stringBuffer = new StringBuffer("\"/*\"");
		assertEquals("\"/*\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentInQuotesWithEscapedQuote() {
		StringBuffer stringBuffer = new StringBuffer("\"\\\"//something\"");
		assertEquals("\"\\\"//something\"", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeLineCommentWithQuotes() {
		StringBuffer stringBuffer = new StringBuffer("//something\"\nnew line");
		assertEquals("\nnew line", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeMultilineCommentWithQuote() {
		StringBuffer stringBuffer = new StringBuffer("/* something \"*/");
		assertEquals("", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenSingleQuote() {
		StringBuffer stringBuffer = new StringBuffer("'");
		assertEquals("'", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenSingleQuoteAtEnd() {
		StringBuffer stringBuffer = new StringBuffer("some'");
		assertEquals("some'", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenSingleQuotes() {
		StringBuffer stringBuffer = new StringBuffer("''");
		assertEquals("''", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenSingleQuotesWithString() {
		StringBuffer stringBuffer = new StringBuffer("'df'");
		assertEquals("'df'", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuoteInSingleQuotes() {
		StringBuffer stringBuffer = new StringBuffer("'\"'");
		assertEquals("'\"'", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeSingleQuoteInSingleQuotes() {
		StringBuffer stringBuffer = new StringBuffer("'\''");
		assertEquals("'\''", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeQuoteInSingleQuotesLineComment() {
		StringBuffer stringBuffer = new StringBuffer("'\"'//some");
		assertEquals("'\"'", b.normalize(stringBuffer).toString());
	}

	@Test
	public void testNormalizeBrokenQuoteInSingleQuotesLineComment() {
		StringBuffer stringBuffer = new StringBuffer("'\"//some");
		assertEquals("'\"//some", b.normalize(stringBuffer).toString());
	}
}
