/*
 * Copyright 2009, 2013, 2020-2021, 2025 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.template;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.util.Configuration;

/**
 * Factory for the template-class(es)
 * @author Sven Strickroth
 *
 */
public class TemplateFactory {
	/**
	 * Returns the a template instance
	 * @param servletRequest
	 * @param servletResponse
	 * @return a template-object
	 * @throws IOException
	 */
	public static Template getTemplate(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		try {
			return Configuration.getInstance().getTemplateConstructor().newInstance(servletRequest, servletResponse);
		} catch (Exception e) {
			LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()).error("Could not instantiate template", e);
			throw new RuntimeException("Could not instantiate template");
		}
	}
}
