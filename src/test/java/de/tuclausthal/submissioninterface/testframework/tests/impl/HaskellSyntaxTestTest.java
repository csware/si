/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Esat Avci <e.avci@campus.lmu.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.testanalyzer.haskell.syntax.RegexBasedHaskellClustering;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

public class HaskellSyntaxTestTest {

	private HaskellSyntaxTest haskellSyntaxTest;

	@BeforeEach
	void setUp() {
		var haskellSyntaxTestEntity = new de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest();
		haskellSyntaxTest = new HaskellSyntaxTest(haskellSyntaxTestEntity);
	}

	@Test
	void testHaskellSyntaxTestOK() {
		var result = new TestExecutorTestResult();
		var stdout = new StringBuffer("Test");
		var stderr = new StringBuffer("warning: something something");
		haskellSyntaxTest.analyzeAndSetResult(true, stdout, stderr, 0, false, result);
		assertTrue(result.isTestPassed(), "Test should pass when stderr doesn't contain 'error:'.");
	}

	@Test
	void testHaskellSyntaxTestSyntaxError() {
		var result = new TestExecutorTestResult();
		var stdout = new StringBuffer("test");
		var stderr = new StringBuffer("[1 of 1] Compiling Main             ( Main.hs, interpreted )\n\nMain.hs:3:1: error:\n    parse error on input `='");
		haskellSyntaxTest.analyzeAndSetResult(true, stdout, stderr, 1, false, result);
		assertFalse(result.isTestPassed(), "Test should fail when stderr contains 'error:'.");
	}

	@Test
	void testHaskellSyntaxTestAnotherError() {
		var result = new TestExecutorTestResult();
		var stdout = new StringBuffer("test");
		var stderr = new StringBuffer("Test:1:1-12: error:\n    Parse error in pattern: test In a function binding for the ‘-’ operator.\n | |\n 8 | test 0 = 1\n |    ^^^^^^^^^");
		haskellSyntaxTest.analyzeAndSetResult(true, stdout, stderr, 1, false, result);
		assertFalse(result.isTestPassed(), "Test should fail for any stderr containing 'error:'.");
	}

	@Test
	void testRegexBasedHaskellClustering() {
		String stderr = "Test:1:1-12: error:\n   Parse error in pattern: test In a function binding for the ‘-’ operator.\n |\n 8 | test 0 = 1\n |    ^^^^^^^^^";
		String cluster = RegexBasedHaskellClustering.classify(stderr);
		assertEquals("Parse-Fehler", cluster, "The stderr should be classified as 'Parse-Fehler'.");
	}

	@Test
	void testRegexBasedHaskellClusteringComplex() {
		String stderr = "Test:6:1-8: warning: [-Wtabs]\n   Tab character found here, and in two further locations.\n   Suggested fix: Please use spaces instead.\n |\n6 |         where test f [] acc =acc\n | ^^^^^^^^\n\nTest:14:26-33: error:\n   • Expected kind ‘* -> * -> Constraint’, but ‘Test’ has kind ‘*’\n   • In the instance declaration for ‘Test a b’\n  |\n14 | instance (Eq a, Eq b) => Test a b  where\n  |                          ^^^^^^^^Test:7:51: error: parse error on input ‘=’\n |\n7 |                   Test f (x:xs) (first, second) = test f xs (first ++ [fst (f x)], second ++ [snd (f x)])\n |                                                   ^";
		String cluster = RegexBasedHaskellClustering.classify(stderr);
		assertEquals("Parse-Fehler", cluster, "The stderr should be classified as 'Parse-Fehler'.");
	}
}

