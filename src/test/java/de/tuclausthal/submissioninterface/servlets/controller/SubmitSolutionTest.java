/*
 * Copyright 2021, 2023-2024 Sven Strickroth <email@cs-ware.de>
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import javax.servlet.http.Part;

import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.helpers.NOPLoggerFactory;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

public class SubmitSolutionTest {
	private static class MyPartImpl implements Part {
		final private Path file;

		public MyPartImpl(final Path file) {
			this.file = file;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return Files.newInputStream(file);
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
	void testHandleUploadedFileZIPWithUmlaut(@TempDir final Path destDir) throws IOException {
		Task task = new Task();
		task.setArchiveFilenameRegexp(".+\\.txt");

		final Path file = Path.of("src/test/resources/windows-zip-umlaut.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "ümlauts.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		final Path createdFile = destDir.resolve("ümlaut" + File.separator + "Ümlaut.txt");
		assertTrue(Files.exists(createdFile));
		assertEquals(25, Files.size(createdFile));
	}

	@Test
	void testHandleUploadedFileZIPWithUmlautUTF8(@TempDir final Path destDir) throws IOException {
		Task task = new Task();
		task.setArchiveFilenameRegexp(".+\\.txt");

		final Path file = Path.of("src/test/resources/umlaut-utf-8.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "ümlauts.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		final Path createdFile = destDir.resolve("├╝mlaut" + File.separator + "├£mlaut.txt");
		assertTrue(Files.exists(createdFile));
		assertEquals(25, Files.size(createdFile));
	}

	@Test
	void testHandleUploadedFileJava(@TempDir final Path destDir) throws IOException {
		Task task = new Task();

		final Path file = Path.of("src/test/resources/TriangleOutput.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		final Path createdFile = destDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile));
		assertEquals(663, Files.size(createdFile));
		assertTrue(PathUtils.fileContentEquals(file, createdFile));
	}

	@Test
	void testHandleUploadedFileJavaCP850(@TempDir final Path destDir) throws IOException {
		Task task = new Task();

		final Path file = Path.of("src/test/resources/TriangleOutputCP850.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		final Path createdFile = destDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile));
		assertEquals(660, Files.size(createdFile));
		assertTrue(PathUtils.fileContentEquals(file, createdFile));
	}

	@Test
	void testHandleUploadedFileJavaWithPackage(@TempDir final Path destDir) throws IOException {
		Task task = new Task();

		final Path file = Path.of("src/test/resources/TriangleOutputPackage.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		final Path createdFile = destDir.resolve("mypackage" + File.separator + "TriangleOutput.java");
		assertTrue(Files.exists(createdFile));
		assertEquals(685, Files.size(createdFile));
		assertTrue(PathUtils.fileContentEquals(file, createdFile));
	}

	@Test
	void testHandleUploadedFileJavaWithBrokenPackage(@TempDir final Path destDir) throws IOException {
		Task task = new Task();

		final Path file = Path.of("src/test/resources/TriangleOutputPackageCommented.java");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "TriangleOutput.java", new MyPartImpl(file), null)));
		final Path createdFile = destDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile));
		assertEquals(687, Files.size(createdFile));
		assertTrue(PathUtils.fileContentEquals(file, createdFile));
	}

	@Test
	void testHandleUploadedFileZIP(@TempDir final Path destDir) throws IOException {
		Task task = new Task();
		task.setArchiveFilenameRegexp(".+");

		final Path file = Path.of("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(null, destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(3, numberOfFiles(destDir));
		final Path createdFile1 = destDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile1));
		assertEquals(663, Files.size(createdFile1));
		assertTrue(PathUtils.fileContentEquals(Path.of("src/test/resources/TriangleOutput.java"), createdFile1));
		final Path packageDir = destDir.resolve("mypackage");
		assertEquals(1, numberOfFiles(packageDir));
		final Path createdFile2 = packageDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile2));
		assertEquals(685, Files.size(createdFile2));
		assertTrue(PathUtils.fileContentEquals(Path.of("src/test/resources/TriangleOutputPackage.java"), createdFile2));
		final Path createdFile3 = destDir.resolve("someother-file.txt");
		assertTrue(Files.exists(createdFile3));
		assertEquals(14, Files.size(createdFile3));
	}

	@Test
	void testHandleUploadedFileZIPFilenameFilter(@TempDir final Path destDir) throws IOException {
		Task task = new Task();
		task.setArchiveFilenameRegexp("TriangleOutput\\.java");

		final Path file = Path.of("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertFalse(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(2, numberOfFiles(destDir));
		final Path createdFile1 = destDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile1));
		assertEquals(663, Files.size(createdFile1));
		final Path packageDir = destDir.resolve("mypackage");
		assertEquals(1, numberOfFiles(packageDir));
		final Path createdFile2 = packageDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile2));
		assertEquals(685, Files.size(createdFile2));
		final Path createdFile3 = destDir.resolve("someother-file.txt");
		assertFalse(Files.exists(createdFile3));
	}

	@Test
	void testHandleUploadedFileZIPFileFilter(@TempDir final Path destDir) throws IOException {
		Task task = new Task();
		task.setArchiveFilenameRegexp("^TriangleOutput\\.java");

		final Path file = Path.of("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertFalse(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(1, numberOfFiles(destDir));
		final Path createdFile1 = destDir.resolve("TriangleOutput.java");
		assertTrue(Files.exists(createdFile1));
		assertEquals(663, Files.size(createdFile1));
		assertTrue(PathUtils.fileContentEquals(Path.of("src/test/resources/TriangleOutput.java"), createdFile1));
		final Path packageDir = destDir.resolve("mypackage");
		assertFalse(Files.exists(packageDir));
		final Path createdFile3 = destDir.resolve( "someother-file.txt");
		assertFalse(Files.exists(createdFile3));
	}

	@Test
	void testHandleUploadedFileZIPNoUnpack(@TempDir final Path destDir) throws IOException {
		Task task = new Task();
		task.setArchiveFilenameRegexp("-");

		final Path file = Path.of("src/test/resources/zip-file.zip");
		assertDoesNotThrow(() -> assertTrue(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(1, numberOfFiles(destDir));
		final Path createdFile = destDir.resolve("some-zip.zip");
		assertTrue(Files.exists(createdFile));
		assertEquals(3104, Files.size(createdFile));
		assertTrue(PathUtils.fileContentEquals(file, createdFile));
	}

	public static long numberOfFiles(final Path path) throws IOException {
		if (!Files.isDirectory(path)) {
			throw new NotDirectoryException(path.toString());
		}
		try (Stream<Path> pathStream = Files.list(path)) {
			return pathStream.count();
		}
	}

	@Test
	void testHandleUploadedFileZIPWithRelativePathUp(@TempDir final Path tempDir) throws IOException {
		final Path destDir = tempDir.resolve("destination");
		Files.createDirectory(destDir);

		Task task = new Task();
		task.setArchiveFilenameRegexp(".+");

		final Path file = Path.of("src/test/resources/zip-rel-one-up.zip");
		assertDoesNotThrow(() -> assertFalse(SubmitSolution.handleUploadedFile(new NOPLoggerFactory().getLogger(""), destDir, task, "some-zip.zip", new MyPartImpl(file), Charset.forName("cp850"))));
		assertEquals(2, numberOfFiles(destDir));
		final Path createdFile1 = destDir.resolve("simple1.txt");
		assertTrue(Files.isRegularFile(createdFile1));
		final Path createdFile2 = destDir.resolve("child/simple2.txt");
		assertTrue(Files.isRegularFile(createdFile2));
		assertEquals(1, numberOfFiles(tempDir));
		final Path createdFile3 = tempDir.resolve("simple3.txt");
		assertFalse(Files.exists(createdFile3));
	}
}
