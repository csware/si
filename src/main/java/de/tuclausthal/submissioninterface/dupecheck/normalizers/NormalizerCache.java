/*
 * Copyright 2009-2010, 2017, 2020-2021, 2023-2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dupecheck.normalizers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import de.tuclausthal.submissioninterface.util.Util;

/**
 * A normalizer cache
 * @author Sven Strickroth
 */
public class NormalizerCache {
	final private NormalizerIf normalizer;
	final private Path cacheDirectoty;
	final private Path pathToTask;

	/**
	 * Creates a new normalizer cache
	 * @param pathToTask 
	 * @param normalizer the normalizer to cache
	 * @throws IOException
	 */
	public NormalizerCache(final Path pathToTask, final NormalizerIf normalizer) throws IOException {
		this.pathToTask = pathToTask;
		this.normalizer = normalizer;
		cacheDirectoty = Util.createTemporaryDirectory("normalizer.cache");
		if (cacheDirectoty == null) {
			throw new IOException("Could not create temporary directory");
		}
	}

	/**
	 * Normalize and cache the contents of the given file of the given submission
	 * @param file
	 * @return the (cached) normalized string
	 * @throws IOException
	 */
	synchronized public StringBuffer normalize(String file) throws IOException {
		final Path tempFile = cacheDirectoty.resolve(file);
		if (Files.isRegularFile(tempFile)) {
			return Util.loadFile(tempFile);
		}
		final StringBuffer stringBuffer = normalizer.normalize(Util.loadFile(pathToTask.resolve(file)));
		Files.createDirectories(tempFile.getParent());
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(tempFile)))) {
			bw.write(stringBuffer.toString());
		}
		return stringBuffer;
	}

	/**
	 * Cleanup cache
	 */
	public void cleanUp() {
		if (cacheDirectoty != null) {
			Util.recursiveDelete(cacheDirectoty);
		}
	}
}
