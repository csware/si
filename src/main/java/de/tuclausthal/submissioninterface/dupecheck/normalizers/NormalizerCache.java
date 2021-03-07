/*
 * Copyright 2009-2010, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dupecheck.normalizers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.tuclausthal.submissioninterface.util.Util;

/**
 * A normalizer cache
 * @author Sven Strickroth
 */
public class NormalizerCache {
	private NormalizerIf normalizer;
	private File cacheDirectoty;
	private File pathToTask;

	/**
	 * Creates a new normalizer cache
	 * @param pathToTask 
	 * @param normalizer the normalizer to cache
	 * @throws IOException
	 */
	public NormalizerCache(File pathToTask, NormalizerIf normalizer) throws IOException {
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
		File tempFile = new File(cacheDirectoty, file);
		if (tempFile.exists()) {
			return Util.loadFile(tempFile);
		}
		StringBuffer stringBuffer = normalizer.normalize(Util.loadFile(new File(pathToTask, file)));
		tempFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
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
