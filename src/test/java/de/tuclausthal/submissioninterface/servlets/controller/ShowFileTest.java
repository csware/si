/*
 * Copyright 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShowFileTest {

	@Test
	void testIsInlineAble() {
		assertTrue(ShowFile.isInlineAble("file.txt"));
		assertTrue(ShowFile.isInlineAble("file.pdf.txt"));
		assertTrue(ShowFile.isInlineAble("file.pdf"));
		assertTrue(ShowFile.isInlineAble("file.jpg"));
		assertTrue(ShowFile.isInlineAble("Class.java"));
		assertFalse(ShowFile.isInlineAble("File.jPG")); // check is case sensitive! API requires lowerCase
		assertFalse(ShowFile.isInlineAble("File.doc"));
		assertFalse(ShowFile.isInlineAble("File.txt.doc"));
		assertFalse(ShowFile.isInlineAble(""));
	}

}
