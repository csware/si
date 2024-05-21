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

package de.tuclausthal.submissioninterface.util;

public enum TaskPath {
	LOGS("logs", false),
	ADVISORFILES("advisorfiles", true),
	MODELSOLUTIONFILES("modelsolutionfiles", true);

	private String path;
	private boolean allowImportExport;

	private TaskPath(final String path, final boolean allowImportExport) {
		this.path = path;
		this.allowImportExport = allowImportExport;
	}

	public String getPathComponent() {
		return path;
	}

	public boolean isAllowImportExport() {
		return allowImportExport;
	}
}
