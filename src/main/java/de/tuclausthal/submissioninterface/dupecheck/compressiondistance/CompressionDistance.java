/*
 * Copyright 2009, 2020, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dupecheck.compressiondistance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import SevenZip.Compression.LZMA.Encoder;
import de.tuclausthal.submissioninterface.dupecheck.DupeCheck;

/**
 * Compression-distance plagiarism test implementation
 * @author Sven Strickroth
 */
public class CompressionDistance extends DupeCheck {
	private static ThreadLocal<Encoder> encoder = new ThreadLocal<>();

	public CompressionDistance(final Path path) {
		super(path);
	}

	@Override
	protected int calculateSimilarity(StringBuffer fileOne, StringBuffer fileTwo, int maximumDifferenceInPercent) throws IOException {
		if (encoder.get() == null) {
			encoder.set(new Encoder());
		}
		return compressionDistance(fileOne, fileTwo);
	}

	/**
	 * Compress a stringbuffer and return the length of the compressed string
	 * @param sb the string to compress
	 * @return length of the compressed string
	 * @throws IOException
	 */
	private static int compress(final StringBuffer sb) throws IOException {
		final ByteArrayInputStream errorInputStream = new ByteArrayInputStream(sb.toString().getBytes());
		final ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
		encoder.get().Code(errorInputStream, errorOutputStream, -1, -1, null);
		return errorOutputStream.size();
	}

	/**
	 * Calculates the distance of two stings with the compression-distance
	 * @param fileOne
	 * @param fileTwo
	 * @return the similarity in per cent
	 * @throws IOException
	 */
	private static int compressionDistance(final StringBuffer fileOne, final StringBuffer fileTwo) throws IOException {
		// TODO: caching possible here
		final int one = compress(fileOne);
		final int two = compress(fileTwo);
		final StringBuffer fileBoth = new StringBuffer(fileOne);
		fileBoth.append(fileTwo);
		final int both = compress(fileBoth);
		return (int) ((1 - (double) (both - Math.min(one, two)) / Math.max(one, two)) * 100.0);
	}
}
