/*
 * Copyright 2022-2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ParameterHelper {
	final private List<Parameter> parameters = new ArrayList<>();
	final private String baseURI;
	final private HttpServletRequest request;

	public ParameterHelper(HttpServletRequest request, String baseURI) {
		this.request = request;
		this.baseURI = baseURI;
	}

	public boolean register(Parameter parameter) {
		parameters.add(parameter);
		return request.getParameter(parameter.key) != null;
	}

	public void constructLinks(final HttpServletResponse response, final PrintWriter out) {
		out.println("<ul>");
		for (final Parameter parameter : parameters) {
			out.print("<li><a href=\"");
			out.print(Util.generateHTMLLink(constructParameters(parameter), response));
			out.print("\">");
			out.print(parameter.getTitle());
			out.print(" ");
			if (request.getParameter(parameter.getKey()) != null) {
				out.print(parameter.getDeactivateTitle());
			} else {
				out.print(parameter.getActivateTitle());
			}
			out.println("</a></li>");
		}
		out.println("</ul>");
	}

	private String constructParameters(final Parameter current) {
		final StringBuilder sb = new StringBuilder();
		sb.append(baseURI);
		for (final Parameter parameter : parameters) {
			if (parameter == current || request.getParameter(parameter.getKey()) == null) {
				continue;
			}
			sb.append("&");
			sb.append(parameter.getKey());
		}
		if (request.getParameter(current.getKey()) == null) {
			sb.append("&");
			sb.append(current.getKey());
		}
		return sb.toString();
	}

	public static class Parameter {
		final private String key;
		final private String title;
		private boolean inverted = false;
		private String show = "anzeigen";
		private String hide = "ausblenden";

		public Parameter(String key, String title) {
			this.key = key;
			this.title = title;
			inverted = !key.startsWith("show");
		}

		public Parameter(String key, String title, String show, String hide) {
			this.key = key;
			this.title = title;
			this.show = show;
			this.hide = hide;
		}

		public String getKey() {
			return key;
		}

		public String getTitle() {
			return title;
		}

		public String getActivateTitle() {
			if (inverted) {
				return hide;
			}
			return show;
		}

		public String getDeactivateTitle() {
			if (inverted) {
				return show;
			}
			return hide;
		}
	}
}
