/*
 * Copyright 2021, 2023 Sven Strickroth <email@cs-ware.de>
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.helpers.NOPLoggerFactory;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

public class SubmitSolutionTest {
	private static class MyPartImpl implements Part {
		private File file;

		public MyPartImpl(File file) {
			this.file = file;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(file);
		}

		@Override
		public String getContentType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getSubmittedFileName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getSize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void write(String fileName) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getHeader(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Collection<String> getHeaders(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Collection<String> getHeaderNames() {
			throw new UnsupportedOperationException();
		}
	}

	@Test
	void testHandleUploadedFileZIPWithUmlaut(@TempDir File destDir) {
		Task task = new Task();
		task.setArchiveFilenameRegexp(".+\\.txt");

		final File file = new File("src/test/resources/windows-zip-umlaut.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "ümlauts.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		File createdFile = new File(destDir, "ümlaut" + File.separator + "Ümlaut.txt");
		assertTrue(createdFile.exists());
		assertEquals(25, createdFile.length());
	}

	@Test
	void testHandleUploadedFileZIPWithUmlautUTF8(@TempDir File destDir) {
		Task task = new Task();
		task.setArchiveFilenameRegexp(".+\\.txt");

		final File file = new File("src/test/resources/umlaut-utf-8.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "ümlauts.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		File createdFile = new File(destDir, "├╝mlaut" + File.separator + "├£mlaut.txt");
		assertTrue(createdFile.exists());
		assertEquals(25, createdFile.length());
	}

	@Test
	void testHandleUploadedFileJava(@TempDir File destDir) {
		Task task = new Task();

		final File file = new File("src/test/resources/TriangleOutput.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		File createdFile = new File(destDir, "TriangleOutput.java");
		assertTrue(createdFile.exists());
		assertEquals(663, createdFile.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(file, createdFile)));
	}

	@Test
	void testHandleUploadedFileJavaCP850(@TempDir File destDir) {
		Task task = new Task();

		final File file = new File("src/test/resources/TriangleOutputCP850.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		File createdFile = new File(destDir, "TriangleOutput.java");
		assertTrue(createdFile.exists());
		assertEquals(660, createdFile.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(file, createdFile)));
	}

	@Test
	void testHandleUploadedFileJavaWithPackage(@TempDir File destDir) {
		Task task = new Task();

		final File file = new File("src/test/resources/TriangleOutputPackage.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		File createdFile = new File(destDir, "mypackage" + File.separator + "TriangleOutput.java");
		assertTrue(createdFile.exists());
		assertEquals(685, createdFile.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(file, createdFile)));
	}

	@Test
	void testHandleUploadedFileJavaWithBrokenPackage(@TempDir File destDir) {
		Task task = new Task();

		final File file = new File("src/test/resources/TriangleOutputPackageCommented.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		File createdFile = new File(destDir, "TriangleOutput.java");
		assertTrue(createdFile.exists());
		assertEquals(687, createdFile.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(file, createdFile)));
	}

	@Test
	void testHandleUploadedFileZIP(@TempDir File destDir) {
		Task task = new Task();
		task.setArchiveFilenameRegexp(".+");

		final File file = new File("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(3, destDir.listFiles().length);
		File createdFile1 = new File(destDir, "TriangleOutput.java");
		assertTrue(createdFile1.exists());
		assertEquals(663, createdFile1.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(new File("src/test/resources/TriangleOutput.java"), createdFile1)));
		File packageDir = new File(destDir, "mypackage");
		assertEquals(1, packageDir.listFiles().length);
		File createdFile2 = new File(packageDir, "TriangleOutput.java");
		assertTrue(createdFile2.exists());
		assertEquals(685, createdFile2.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(new File("src/test/resources/TriangleOutputPackage.java"), createdFile2)));
		File createdFile3 = new File(destDir, "someother-file.txt");
		assertTrue(createdFile3.exists());
		assertEquals(14, createdFile3.length());
	}

	@Test
	void testHandleUploadedFileZIPFilenameFilter(@TempDir File destDir) {
		Task task = new Task();
		task.setArchiveFilenameRegexp("TriangleOutput\\.java");

		final File file = new File("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertFalse(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(2, destDir.listFiles().length);
		File createdFile1 = new File(destDir, "TriangleOutput.java");
		assertTrue(createdFile1.exists());
		assertEquals(663, createdFile1.length());
		File packageDir = new File(destDir, "mypackage");
		assertEquals(1, packageDir.listFiles().length);
		File createdFile2 = new File(packageDir, "TriangleOutput.java");
		assertTrue(createdFile2.exists());
		assertEquals(685, createdFile2.length());
		File createdFile3 = new File(destDir, "someother-file.txt");
		assertFalse(createdFile3.exists());
	}

	@Test
	void testHandleUploadedFileZIPFileFilter(@TempDir File destDir) {
		Task task = new Task();
		task.setArchiveFilenameRegexp("^TriangleOutput\\.java");

		final File file = new File("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertFalse(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(1, destDir.listFiles().length);
		File createdFile1 = new File(destDir, "TriangleOutput.java");
		assertTrue(createdFile1.exists());
		assertEquals(663, createdFile1.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(new File("src/test/resources/TriangleOutput.java"), createdFile1)));
		File packageDir = new File(destDir, "mypackage");
		assertFalse(packageDir.exists());
		File createdFile3 = new File(destDir, "someother-file.txt");
		assertFalse(createdFile3.exists());
	}

	@Test
	void testHandleUploadedFileZIPNoUnpack(@TempDir File destDir) {
		Task task = new Task();
		task.setArchiveFilenameRegexp("-");

		final File file = new File("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(1, destDir.listFiles().length);
		File createdFile = new File(destDir, "some-zip.zip");
		assertTrue(createdFile.exists());
		assertEquals(3104, createdFile.length());
		assertDoesNotThrow(() -> assertTrue(FileUtils.contentEquals(file, createdFile)));
	}

	@Test
	void testHandleUploadedFileZIPWithRelativePathUp(@TempDir File tempDir) {
		final File destDir = new File(tempDir, "destination");
		assertTrue(destDir.mkdir());

		Task task = new Task();
		task.setArchiveFilenameRegexp(".+");

		final File file = new File("src/test/resources/zip-rel-one-up.zip");
		assertDoesNotThrow(() -> assertFalse(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(2, destDir.listFiles().length);
		File createdFile1 = new File(destDir, "simple1.txt");
		assertTrue(createdFile1.isFile());
		File createdFile2 = new File(destDir, "child/simple2.txt");
		assertTrue(createdFile2.isFile());
		assertEquals(1, tempDir.listFiles().length);
		File createdFile3 = new File(tempDir, "simple3.txt");
		assertFalse(createdFile3.exists());
	}
}
