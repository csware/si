/*
 * Copyright 2021, 2024 Sven Strickroth <email@cs-ware.de>
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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class CrLfFilterReader extends FilterReader {
	public CrLfFilterReader(Reader in) {
		super(in);
		assert (in.markSupported());
	}

	@Override
	public int read() throws IOException {
		final int thisChar = super.read();
		if (thisChar == '\r') {
			super.mark(1);
			final int nextChar = super.read();
			if (nextChar == -1) {
				return thisChar;
			} else if (nextChar != '\n') {
				super.reset();
				return thisChar;
			}
			return nextChar;
		}
		return thisChar;
	}

	@Override
	public int read(char[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	// based on FixCrLfFilter.java from the Apache Ant project
	@Override
	public int read(final char[] buf, int start, int length) throws IOException {
		int count = 0;
		int c = 0;
		while (length-- > 0 && (c = this.read()) != -1) {
			buf[start++] = (char) c;
			count++;
		}
		// if at EOF with no characters in the buffer, return EOF
		return (count == 0 && c == -1) ? -1 : count;
	}
}
