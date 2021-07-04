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
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <select size=\"1\" name=\"cloze0\"><option value=\"\"></option><option value=\"Kalifornien\">Kalifornien</option><option value=\"Arizona\">Arizona</option></select>\n* Tucson: <select size=\"1\" name=\"cloze1\"><option value=\"\"></option><option value=\"Kalifornien\">Kalifornien</option><option value=\"Arizona\">Arizona</option></select>\n* Los Angeles: <select size=\"1\" name=\"cloze2\"><option value=\"\"></option><option value=\"Kalifornien\">Kalifornien</option><option value=\"Arizona\">Arizona</option></select>\n* Phoenix: <select size=\"1\" name=\"cloze3\"><option value=\"\"></option><option value=\"Kalifornien\">Kalifornien</option><option value=\"Arizona\">Arizona</option></select>\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" autocomplete=\"off\" />.", ch.toHTML());
	}

	@Test
	public void testRenderingNotAnsweredNonEditable() {
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", null, true, false);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" />\n* Tucson: <input name=\"cloze1\" type=\"text\" disabled=\"disabled\" />\n* Los Angeles: <input name=\"cloze2\" type=\"text\" disabled=\"disabled\" />\n* Phoenix: <input name=\"cloze3\" type=\"text\" disabled=\"disabled\" />\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" disabled=\"disabled\" autocomplete=\"off\" />.", ch.toHTML());
	}

	@Test
	public void testRenderingOldResultsNonEditable() {
		List<String> results = Arrays.asList("Kalifornien", "Arizona", "Kalifornien", "Kalifornien", "Paris");
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", results, true, false);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"Kalifornien\" />\n* Tucson: <input name=\"cloze1\" type=\"text\" disabled=\"disabled\" value=\"Arizona\" />\n* Los Angeles: <input name=\"cloze2\" type=\"text\" disabled=\"disabled\" value=\"Kalifornien\" />\n* Phoenix: <input name=\"cloze3\" type=\"text\" disabled=\"disabled\" value=\"Kalifornien\" />\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" disabled=\"disabled\" value=\"Paris\" autocomplete=\"off\" />.", ch.toHTML());
		assertEquals(400, ch.calculatePoints(results));
	}

	@Test
	public void testRenderingOldResultsEditable() {
		List<String> results = Arrays.asList("Kalifornien", "Arizona", "Kalifornien", "Kalifornien", "Something");
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Arizona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", results, false, false);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <select size=\"1\" name=\"cloze0\"><option value=\"\"></option><option value=\"Kalifornien\" selected=\"selected\">Kalifornien</option><option value=\"Arizona\">Arizona</option></select>\n* Tucson: <select size=\"1\" name=\"cloze1\"><option value=\"\"></option><option value=\"Kalifornien\">Kalifornien</option><option value=\"Arizona\" selected=\"selected\">Arizona</option></select>\n* Los Angeles: <select size=\"1\" name=\"cloze2\"><option value=\"\"></option><option value=\"Kalifornien\" selected=\"selected\">Kalifornien</option><option value=\"Arizona\">Arizona</option></select>\n* Phoenix: <select size=\"1\" name=\"cloze3\"><option value=\"\"></option><option value=\"Kalifornien\" selected=\"selected\">Kalifornien</option><option value=\"Arizona\">Arizona</option></select>\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" value=\"Something\" autocomplete=\"off\" />.", ch.toHTML());
		assertEquals(300, ch.calculatePoints(results));
	}

	@Test
	public void testRenderingOldResultsFeedback() {
		List<String> results = Arrays.asList("Kalifornien", "Ar&i\"z>ona", "Kalifornien", "Kalifornien", "Marseilles");
		ClozeTaskType ch = new ClozeTaskType("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Tucson: {MULTICHOICE:0=Kalifornien~1=Ar&i\"z>ona}\n* Los Angeles: {MULTICHOICE:1=Kalifornien~0=Arizona}\n* Phoenix: {MULTICHOICE:0=Kalifornien~1=Arizona}\nDie Hauptstadt von Frankreich ist {SHORTANSWER:1=Paris~.5=Marseilles}.", results, true, true);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("Ordnen Sie die folgenden Städte den richtigen US-Bundesstaaten zu:\n* San Francisco: <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"Kalifornien\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\n* Tucson: <input name=\"cloze1\" type=\"text\" disabled=\"disabled\" value=\"Ar&amp;i&#34;z&gt;ona\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\n* Los Angeles: <input name=\"cloze2\" type=\"text\" disabled=\"disabled\" value=\"Kalifornien\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\n* Phoenix: <input name=\"cloze3\" type=\"text\" disabled=\"disabled\" value=\"Kalifornien\" /> <span class=\"cloze_points\">(➜ 0 Punkt(e))</span>\nDie Hauptstadt von Frankreich ist <input name=\"cloze4\" type=\"text\" disabled=\"disabled\" value=\"Marseilles\" autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0,5 Punkt(e))</span>.", ch.toHTML());
		assertEquals(350, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerNoCaseFirst() {
		List<String> results = Arrays.asList("perl");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER_NC:1=Perl~1=PHP~0.5=HTML}", results, false, false);
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" value=\"perl\" autocomplete=\"off\" />", ch.toHTML());
		assertEquals("Perl: 1, PHP: 1, HTML: 0.5", ch.getCorrect(0));
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerNoCaseSecond() {
		List<String> results = Arrays.asList("pHp");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER_NC:1=Perl~1=PHP~0.5=HTML}", results, false, false);
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" value=\"pHp\" autocomplete=\"off\" />", ch.toHTML());
		assertEquals("Perl: 1, PHP: 1, HTML: 0.5", ch.getCorrect(0));
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringShortAnswerNoCaseLesspoints() {
		List<String> results = Arrays.asList("HTML");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER_NC:1=Perl~1=PHP~0.5=HTML}", results, false, false);
		assertTrue(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" value=\"HTML\" autocomplete=\"off\" />", ch.toHTML());
		assertEquals("Perl: 1, PHP: 1, HTML: 0.5", ch.getCorrect(0));
		assertEquals(50, ch.calculatePoints(results));
	}

	@Test
	public void testRendgeringNotAutoGradeAble() {
		List<String> results = Arrays.asList("Perl");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER:}", results, false, false);
		assertFalse(ch.isAutoGradeAble());
		assertFalse(ch.isAutoGradeAble(0));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" value=\"Perl\" autocomplete=\"off\" />", ch.toHTML());
	}

	@Test
	public void testRendgeringMixedAutoGradeAble() {
		List<String> results = Arrays.asList("Perl", "Haskell");
		ClozeTaskType ch = new ClozeTaskType("Nennen Sie eine Programmiersprache: {SHORTANSWER:1=Perl~1=PHP~0.5=HTML}\nNennen Sie eine weitere Programmiersprache: {SHORTANSWER:}", results, true, true);
		assertFalse(ch.isAutoGradeAble());
		assertTrue(ch.isAutoGradeAble(0));
		assertFalse(ch.isAutoGradeAble(1));
		assertEquals("Nennen Sie eine Programmiersprache: <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"Perl\" autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>\nNennen Sie eine weitere Programmiersprache: <input name=\"cloze1\" type=\"text\" disabled=\"disabled\" value=\"Haskell\" autocomplete=\"off\" />", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testNumericResult() {
		List<String> results = Arrays.asList("8.5");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5~.5=8}.", results, true, true);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"8.5\" autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>.", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));
	}

	@Test
	public void testNumericResultRoundingError() {
		List<String> results = Arrays.asList("8.503");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5:.1~.5=8}.", results, true, true);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"8.503\" autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 1 Punkt(e))</span>.", ch.toHTML());
		assertEquals(100, ch.calculatePoints(results));

		List<String> resultsTooBigError = Arrays.asList("8.61");
		assertEquals(0, ch.calculatePoints(resultsTooBigError));
	}

	@Test
	public void testNumericResultSecondResult() {
		List<String> results = Arrays.asList("8");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5~.5=8}.", results, true, true);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"8\" autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0,5 Punkt(e))</span>.", ch.toHTML());
		assertEquals(50, ch.calculatePoints(results));
	}

	@Test
	public void testNumericResultSecondResultRange() {
		List<String> results = Arrays.asList("7");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5:.001~.5=8:1}.", results, true, true);
		assertTrue(ch.isAutoGradeAble());
		assertEquals(50, ch.calculatePoints(results));
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"7\" autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0,5 Punkt(e))</span>.", ch.toHTML());
	}

	@Test
	public void testNumericResultWrong() {
		List<String> results = Arrays.asList("5");
		ClozeTaskType ch = new ClozeTaskType("4+4.5 = {NUMERICAL:1=8.5~.5=8}.", results, true, true);
		assertTrue(ch.isAutoGradeAble());
		assertEquals("4&#43;4.5 &#61; <input name=\"cloze0\" type=\"text\" disabled=\"disabled\" value=\"5\" autocomplete=\"off\" /> <span class=\"cloze_points\">(➜ 0 Punkt(e))</span>.", ch.toHTML());
		assertEquals(0, ch.calculatePoints(results));
	}
}
