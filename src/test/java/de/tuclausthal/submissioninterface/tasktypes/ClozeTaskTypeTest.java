/*
 * Copyright 2021-2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.tasktypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ClozeTaskTypeTest {
	@Test
	public void testRendering() {
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", null, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <select size=\"1\" name=\"cloze0\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\">Arizona</option><option value=\"Kalifornien\">Kalifornien</option></select>\n* Tucson: <select size=\"1\" name=\"cloze1\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\">Arizona</option><option value=\"Kalifornien\">Kalifornien</option></select>\n* Los Angeles: <select size=\"1\" name=\"cloze2\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\">Arizona</option><option value=\"Kalifornien\">Kalifornien</option></select>\n* Phoenix: <select size=\"1\" name=\"cloze3\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\">Arizona</option><option value=\"Kalifornien\">Kalifornien</option></select>\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" class=\"cloze\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />.", ch.toHTML());
	}

	@Test
	public void testRenderingNotAnsweredNonEditable() {
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", null, true, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" size=\"20\" />\n* Tucson: <input name=\"cloze1\" type=\"text\" class=\"cloze\" disabled=\"disabled\" size=\"20\" />\n* Los Angeles: <input name=\"cloze2\" type=\"text\" class=\"cloze\" disabled=\"disabled\" size=\"20\" />\n* Phoenix: <input name=\"cloze3\" type=\"text\" class=\"cloze\" disabled=\"disabled\" size=\"20\" />\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" class=\"cloze\" disabled=\"disabled\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />.", ch.toHTML());
	}

	@Test
	public void testRenderingOldResultsNonEditable() {
		List<String> results = Arrays.asList("Kalifornien", "Arizona", "Kalifornien", "Kalifornien", "Paris");
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", results, true, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" />\n* Tucson: <input name=\"cloze1\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Arizona\" size=\"10\" />\n* Los Angeles: <input name=\"cloze2\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" />\n* Phoenix: <input name=\"cloze3\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" />\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Paris\" size=\"8\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />.", ch.toHTML());
		assertEquals(400, ch.calculatePoints(results));
	}

	@Test
	public void testRenderingOldResultsNonEditableAllOK() {
		List<String> results = Arrays.asList("Kalifornien", "Arizona", "Kalifornien", "Arizona", "Paris");
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", results, true, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" />\n* Tucson: <input name=\"cloze1\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Arizona\" size=\"10\" />\n* Los Angeles: <input name=\"cloze2\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" />\n* Phoenix: <input name=\"cloze3\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Arizona\" size=\"10\" />\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Paris\" size=\"8\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />.", ch.toHTML());
		assertEquals(500, ch.calculatePoints(results));
	}

	@Test
	public void testRenderingOldResultsEditable() {
		List<String> results = Arrays.asList("Kalifornien", "Arizona", "Kalifornien", "Kalifornien", "Something");
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", results, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <select size=\"1\" name=\"cloze0\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\">Arizona</option><option value=\"Kalifornien\" selected=\"selected\">Kalifornien</option></select>\n* Tucson: <select size=\"1\" name=\"cloze1\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\" selected=\"selected\">Arizona</option><option value=\"Kalifornien\">Kalifornien</option></select>\n* Los Angeles: <select size=\"1\" name=\"cloze2\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\">Arizona</option><option value=\"Kalifornien\" selected=\"selected\">Kalifornien</option></select>\n* Phoenix: <select size=\"1\" name=\"cloze3\" class=\"cloze\"><option value=\"\"></option><option value=\"Arizona\">Arizona</option><option value=\"Kalifornien\" selected=\"selected\">Kalifornien</option></select>\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" class=\"cloze\" value=\"Something\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />.", ch.toHTML());
		assertEquals(300, ch.calculatePoints(results));
	}

	@Test
	public void testRenderingOldResultsFeedback() {
		List<String> results = Arrays.asList("Kalifornien", "Ar&i\"z>ona", "Kalifornien", "Kalifornien", "Marseilles");
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Ar&i\"z>ona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", results, true, true);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\n* Tucson: <input name=\"cloze1\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Ar&amp;i&#34;z&gt;ona\" size=\"13\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\n* Los Angeles: <input name=\"cloze2\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\n* Phoenix: <input name=\"cloze3\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Kalifornien\" size=\"14\" /> <span class=\"cloze_points\">(➜ 0 Punkt(e))</span>\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Marseilles\" size=\"13\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0,5 Punkt(e))</span>.", ch.toHTML());
		assertEquals(350, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerNoCaseFirst() {
		List<String> results = Arrays.asList("perl");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER_NC:1=Perl~1=PHP~0.5=HTML}", results, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" class=\"cloze\" value=\"perl\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals("Perl: 1, PHP: 1, HTML: 0.5", ch.getCorrect(0));
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerNoCaseSecond() {
		List<String> results = Arrays.asList("pHp");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER_NC:1=Perl~1=PHP~0.5=HTML}", results, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" class=\"cloze\" value=\"pHp\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals("Perl: 1, PHP: 1, HTML: 0.5", ch.getCorrect(0));
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerNoCaseLesspoints() {
		List<String> results = Arrays.asList("HTML");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER_NC:1=Perl~1=PHP~0.5=HTML}", results, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" class=\"cloze\" value=\"HTML\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals("Perl: 1, PHP: 1, HTML: 0.5", ch.getCorrect(0));
		assertEquals(50, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerIgnoreWhitespace() {
		List<String> results = Arrays.asList("a = b");
		ClozeTaskType ch = new ClozeTaskType("a=1 und b=1, was gilt für a und b? {SHORTANSWER_IS:1=a=b}", results, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("a&#61;1 und b&#61;1, was gilt für a und b? <input name=\"cloze0\" type=\"text\" class=\"cloze\" value=\"a &#61; b\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerIgnoreWhitespaceWrong() {
		List<String> results = Arrays.asList("A = b");
		ClozeTaskType ch = new ClozeTaskType("a=1 und b=1, was gilt für a und b? {SHORTANSWER_IS:1=a=b}", results, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("a&#61;1 und b&#61;1, was gilt für a und b? <input name=\"cloze0\" type=\"text\" class=\"cloze\" value=\"A &#61; b\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals(0, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerNoCaseIgnoreWhitespace() {
		List<String> results = Arrays.asList("A = b");
		ClozeTaskType ch = new ClozeTaskType("a=1 und b=1, was gilt für a und b? {SHORTANSWER_NC_IS:1=a=b}", results, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("a&#61;1 und b&#61;1, was gilt für a und b? <input name=\"cloze0\" type=\"text\" class=\"cloze\" value=\"A &#61; b\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringNotAutoGradeAble() {
		List<String> results = Arrays.asList("Perl");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER:}", results, false, false);
		assertFalse(ch.hasError());
		assertFalse(ch.isAutoGradeAble());
		assertFalse(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" class=\"cloze\" value=\"Perl\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
	}

	@Test
	public void testRendgeringMixedAutoGradeAble() {
		List<String> results = Arrays.asList("Perl", "Haskell");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER:1=Perl~1=PHP~0.5=HTML}\nNennen Sie eine weitere Programmiersprache: {SHORTANSWER:}", results, true, true);
		assertFalse(ch.hasError());
		assertFalse(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertFalse(ch.isAutoGradeAble(1));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Perl\" size=\"7\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\nNennen Sie eine weitere Programmiersprache: <input name=\"cloze1\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Haskell\" size=\"10\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testNumericResult() {
		List<String> results = Arrays.asList("8.5");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5~.5=8}.", results, true, true);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"8.5\" size=\"6\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>.", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testNumericResultRoundingError() {
		List<String> results = Arrays.asList("8.503");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5:.1~.5=8}.", results, true, true);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"8.503\" size=\"8\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>.", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));

		List<String> resultsTooBigError = Arrays.asList("8.61");
		assertEquals(0, ch.calculatePoints(resultsTooBigError));
	}

	@Test
	public void testNumericResultSecondResult() {
		List<String> results = Arrays.asList("8");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5~.5=8}.", results, true, true);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"8\" size=\"4\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0,5 Punkt(e))</span>.", ch.toHTML());
		assertEquals(50, ch.calculatePoints(results));
	}

	@Test
	public void testNumericResultSecondResultRange() {
		List<String> results = Arrays.asList("7");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5:.001~.5=8:1}.", results, true, true);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals(50, ch.calculatePoints(results));
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"7\" size=\"4\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0,5 Punkt(e))</span>.", ch.toHTML());
	}

	@Test
	public void testNumericResultWrong() {
		List<String> results = Arrays.asList("5");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5~.5=8}.", results, true, true);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"5\" size=\"4\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0 Punkt(e))</span>.", ch.toHTML());
		assertEquals(0, ch.calculatePoints(results));
	}

	@Test
	public void testProgramCode() {
		ClozeTaskType ch = new ClozeTaskType("<p>Aufgabenstellung</p><pre>public {SHORTANSWER:1=class} Dreieck {<br />      {SHORTANSWER:1=private} int laengeGrundseite;<br /><br />      // Konstruktor<br />      public {SHORTANSWER:1=Dreieck}(int laengeGrundseite, int hoeheGrundseite) {<br />            {SHORTANSWER:1=this.laengeGrundseite} = laengeGrundseite;<br />            {SHORTANSWER:1=this.hoeheGrundseite} = hoeheGrundseite;<br />      }<br /><br />      public double flaeche() {<br />            {SHORTANSWER:1=return hoeheGrundseite * laengeGrundseite * 0.5~1=return (0.5 * laengeGrundseite * hoeheGrundseite)};<br />      }<br />}</pre>", null, false, false);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals("<p>Aufgabenstellung</p><pre>public <input name=\"cloze0\" type=\"text\" class=\"cloze\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> Dreieck {<!-- --><br />      <input name=\"cloze1\" type=\"text\" class=\"cloze\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> int laengeGrundseite;<br /><br />      // Konstruktor<br />      public <input name=\"cloze2\" type=\"text\" class=\"cloze\" size=\"20\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />(int laengeGrundseite, int hoeheGrundseite) {<!-- --><br />            <input name=\"cloze3\" type=\"text\" class=\"cloze\" size=\"24\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> &#61; laengeGrundseite;<br />            <input name=\"cloze4\" type=\"text\" class=\"cloze\" size=\"23\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> &#61; hoeheGrundseite;<br />      }<br /><br />      public double flaeche() {<!-- --><br />            <input name=\"cloze5\" type=\"text\" class=\"cloze\" size=\"52\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />;<br />      }<br />}</pre>", ch.toHTML());
	}

	@Test
	public void testUnicode() {
		List<String> results = Arrays.asList("5");
		ClozeTaskType ch = new ClozeTaskType("{SHORTANSWER:1=שּׂ}.", results, true, true);
		assertFalse(ch.hasError());
		assertTrue(ch.isAutoGradeAble());
		assertEquals(0, ch.calculatePoints(results));

		assertEquals(100, ch.calculatePoints(Arrays.asList("שּׂ")));
		assertEquals(100, ch.calculatePoints(Arrays.asList("\u05E9\u05C2\u05BC")));
		assertEquals(100, ch.calculatePoints(Arrays.asList("\u05E9\u05BC\u05C2")));
	}

	@Test
	public void testInvalidButNoParseError() {
		ClozeTaskType ch = new ClozeTaskType("<p>Aufgabenstellung</p><pre>public {SHORTANSWER:1=class Dreieck </pre>", null, false, false);
		assertFalse(ch.hasError());
		assertEquals("<p>Aufgabenstellung</p><pre>public {SHORTANSWER:1&#61;class Dreieck </pre>", ch.toHTML());
	}

	@Test
	public void testParseErrorShortAnswer() {
		ClozeTaskType ch = new ClozeTaskType("<p>Aufgabenstellung</p><pre>public {SHORTANSWER:1class~void} Dreieck </pre>", null, false, false);
		assertTrue(ch.hasError());
		assertEquals("Unparseable option \"1class\" found in \"1class~void\"", ch.getError());
	}

	@Test
	public void testParseErrorMultiChoice() {
		ClozeTaskType ch = new ClozeTaskType("<p>Aufgabenstellung</p>{MULTICHOICE:~}", null, false, false);
		assertTrue(ch.hasError());
		assertEquals("No options found in \"~\"", ch.getError());
	}

	@Test
	public void testCalculatePointsTooManyOptions() {
		List<String> results = Arrays.asList("Perl", "Haskell", "C");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER:1=Perl~1=PHP~0.5=HTML}\nNennen Sie eine weitere Programmiersprache: {SHORTANSWER:}", results, true, true);
		assertFalse(ch.hasError());
		assertFalse(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertFalse(ch.isAutoGradeAble(1));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Perl\" size=\"7\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\nNennen Sie eine weitere Programmiersprache: <input name=\"cloze1\" type=\"text\" class=\"cloze\" disabled=\"disabled\" value=\"Haskell\" size=\"10\"" + ClozeTaskType.FIXED_LIMIT + " autocomplete=\"off\" />", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));
	}
}
