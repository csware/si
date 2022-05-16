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

package de.tuclausthal.submissioninterface.servlets;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.servlet.http.HttpServlet;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

public class GATEServletsTest {
	@Test
	public void testViews() {
		Set<Class<?>> viewServlets = new Reflections("de.tuclausthal.submissioninterface.servlets.view").getTypesAnnotatedWith(GATEView.class);
		for (Class<?> servlet : viewServlets) {
			assertTrue(servlet.getSuperclass().equals(HttpServlet.class), "Class " + servlet.getCanonicalName() + " extends HttpServlet");
		}
	}

	@Test
	public void testControllers() {
		Set<Class<?>> controllerServlets = new Reflections("de.tuclausthal.submissioninterface.servlets.controller").getTypesAnnotatedWith(GATEController.class);
		for (Class<?> servlet : controllerServlets) {
			assertTrue(servlet.getSuperclass().equals(HttpServlet.class), "Class " + servlet.getCanonicalName() + " extends HttpServlet");
		}
	}
}
