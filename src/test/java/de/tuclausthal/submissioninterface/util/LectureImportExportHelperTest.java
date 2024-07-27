/*
 * Copyright 2023-2024 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * This program free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonMappingException;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.controller.SubmitSolutionTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaJUnitTest;

@GATEDBTest
public class LectureImportExportHelperTest extends BasicTest {
	@Test
	public void testExportLecture1(@TempDir final Path tempDir) throws IOException {
		final Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		final Path temp = Files.createTempFile(tempDir, "export", ".xml");
		LectureImportExportHelper.exportLecture(session, lecture, Path.of("src/test/resources/lecture1"), temp);

		final Path file = Path.of("src/test/resources/Export Lecture 1 (WS 2020_2021).xml");
		final String knownGood = Files.readString(file).replaceAll("&#xd;\r\n", "&#xd;\n");
		assertArrayEquals(knownGood.getBytes(Charset.forName("UTF-8")), Files.readAllBytes(temp));
	}

	@Test
	public void testExportLecture2(@TempDir final Path tempDir) throws IOException {
		final Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(2);
		final Path temp = Files.createTempFile(tempDir, "export", ".xml");
		LectureImportExportHelper.exportLecture(session, lecture, Path.of("src/test/resources/lecture2"), temp);

		final Path file = Path.of("src/test/resources/Export Lecture 2 Groupwise (WS 2020_2021).xml");
		final String knownGood = Files.readString(file).replaceAll("&#xd;\r\n", "&#xd;\n");
		assertArrayEquals(Files.readAllBytes(temp), knownGood.getBytes(Charset.forName("UTF-8")));
	}

	@Test
	public void testImportLecture1(@TempDir final Path tempRootDir) throws IOException {
		session.beginTransaction();
		final Path tempDir = tempRootDir.resolve("lecture");
		Files.createDirectories(tempDir);
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		final Lecture newLecture;
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export Lecture 1 (WS 2020_2021).xml")) {
			final Map<Task, Set<String>> skippedFiles = new HashMap<>();
			final List<Lecture> lectures = DAOFactory.LectureDAOIf(session).getLectures();
			final int maxId = lectures.stream().mapToInt(l -> l.getId()).max().orElse(0);
			newLecture = LectureImportExportHelper.importLecture(session, null, admin, tempDir, inputStream, skippedFiles);
			assertTrue(skippedFiles.isEmpty(), "no skipped files");
			assertTrue(newLecture.getId() > maxId);
			assertEquals("Lecture 1", newLecture.getName());
			assertEquals(2, newLecture.getTaskGroups().size());
			assertEquals(2, newLecture.getTaskGroups().get(0).getTasks().size());
			assertEquals("<p>Berechnen Sie die Dezimaldarstellung des Bin&auml;r-Wertes $Var0$.</p>", newLecture.getTaskGroups().get(0).getTasks().get(0).getDescription());
			assertTrue(newLecture.getTaskGroups().get(0).getTasks().get(1).isMCTask());
			final List<MCOption> mcOptions = DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(newLecture.getTaskGroups().get(0).getTasks().get(1));
			assertEquals(4, mcOptions.size());
			assertEquals(1, mcOptions.stream().filter(option -> option.isCorrect()).count());
			assertEquals("Wrong 1", mcOptions.get(0).getTitle());
			assertEquals("Wrong 2", mcOptions.get(1).getTitle());
			assertEquals("Correct", mcOptions.get(2).getTitle());
			assertTrue(mcOptions.get(2).isCorrect());
			assertEquals("Wrong 3", mcOptions.get(3).getTitle());

			// recheck with original import file file
			final Path temp = Files.createTempFile(tempRootDir, "export", ".xml");
			LectureImportExportHelper.exportLecture(session, newLecture, tempDir.resolve("lectures").resolve(String.valueOf(newLecture.getId())), temp);
			final Path file = Path.of("src/test/resources/Export Lecture 1 (WS 2020_2021).xml");
			final String knownGood = Files.readString(file).replaceAll("&#xd;\r\n", "&#xd;\n");
			assertArrayEquals(knownGood.getBytes(Charset.forName("UTF-8")), Files.readAllBytes(temp));
		}
		assertEquals(1, SubmitSolutionTest.numberOfFiles(tempDir));
		assertEquals(1, SubmitSolutionTest.numberOfFiles(tempDir.resolve("lectures").resolve(String.valueOf(newLecture.getId()))));
		final Path taskPath = tempDir.resolve("lectures").resolve(String.valueOf(newLecture.getId())).resolve(String.valueOf(newLecture.getTaskGroups().get(1).getTasks().get(0).getTaskid()));
		assertEquals(2, SubmitSolutionTest.numberOfFiles(taskPath));
		assertEquals(1, SubmitSolutionTest.numberOfFiles(taskPath.resolve(TaskPath.ADVISORFILES.getPathComponent())));
		assertEquals(1, SubmitSolutionTest.numberOfFiles(taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent())));
		final String advisorFile = Files.readString(taskPath.resolve(TaskPath.ADVISORFILES.getPathComponent() + "/HelloWorld.java"));
		assertEquals("public class HelloWorld {\r\n	public static void main(String[] args) {\r\n\r\n	}\r\n}\r\n", advisorFile);
		final String modelSolutionFile = Files.readString(taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent() + "/HelloWorld.java"));
		assertEquals("/**\r\n * Class that prints out a greeting to the console\r\n * @author Sven Strickroth\r\n * @version 2.0\r\n */\r\npublic class HelloWorld {\r\n	/**\r\n	 * Prints the greeting message\r\n	 * @param args Command line parameters, not used\r\n	 */\r\n	public static void main(String[] args) {\r\n		System.out.println(greeting());\r\n	}\r\n\r\n	/**\r\n	 * Returns the greeting to print out.\r\n	 * @return the greeting\r\n	 */\r\n	private static String greeting() {\r\n		return \"Hello World!\";\r\n	}\r\n}\r\n", modelSolutionFile);
		session.getTransaction().rollback();
	}

	@Test
	public void testImportLecture2(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export Lecture 2 Groupwise (WS 2020_2021).xml")) {
			final Map<Task, Set<String>> skippedFiles = new HashMap<>();
			final List<Lecture> lectures = DAOFactory.LectureDAOIf(session).getLectures();
			final int maxId = lectures.stream().mapToInt(l -> l.getId()).max().orElse(0);
			final Lecture newLecture = LectureImportExportHelper.importLecture(session, null, admin, tempDir, inputStream, skippedFiles);
			assertTrue(skippedFiles.isEmpty(), "no skipped files");
			assertTrue(newLecture.getId() > maxId);
			assertEquals("Lecture 2 Groupwise", newLecture.getName());
			assertEquals(0, newLecture.getTaskGroups().size());
		}
		assertTrue(PathUtils.isEmptyDirectory(tempDir));
		session.getTransaction().rollback();
	}

	@Test
	public void testImportToLecture2(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(2);
		assertEquals(0, lecture.getTaskGroups().size());
		assertEquals(0, lecture.getGroups().size());
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		final Lecture newLecture;
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export Lecture 1 (WS 2020_2021).xml")) {
			final Map<Task, Set<String>> skippedFiles = new HashMap<>();
			newLecture = LectureImportExportHelper.importLecture(session, lecture, admin, tempDir, inputStream, skippedFiles);
			assertTrue(skippedFiles.isEmpty(), "no skipped files");
			assertEquals("Lecture 2 Groupwise", newLecture.getName());
			assertEquals(2, newLecture.getTaskGroups().size());
			assertEquals(2, newLecture.getTaskGroups().get(0).getTasks().size());
			assertEquals("<p>Berechnen Sie die Dezimaldarstellung des Bin&auml;r-Wertes $Var0$.</p>", newLecture.getTaskGroups().get(0).getTasks().get(0).getDescription());
			assertTrue(newLecture.getTaskGroups().get(0).getTasks().get(1).isMCTask());
			final List<MCOption> mcOptions = DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(newLecture.getTaskGroups().get(0).getTasks().get(1));
			assertEquals(4, mcOptions.size());
			assertEquals(1, mcOptions.stream().filter(option -> option.isCorrect()).count());
			assertEquals("Wrong 1", mcOptions.get(0).getTitle());
			assertEquals("Wrong 2", mcOptions.get(1).getTitle());
			assertEquals("Correct", mcOptions.get(2).getTitle());
			assertTrue(mcOptions.get(2).isCorrect());
			assertEquals("Wrong 3", mcOptions.get(3).getTitle());
			assertEquals(5, newLecture.getGroups().size());
		}
		assertEquals(1, SubmitSolutionTest.numberOfFiles(tempDir));
		assertEquals(1, SubmitSolutionTest.numberOfFiles(tempDir.resolve("lectures").resolve(String.valueOf(newLecture.getId()))));
		final Path taskPath = tempDir.resolve("lectures").resolve(String.valueOf(newLecture.getId())).resolve(String.valueOf(newLecture.getTaskGroups().get(1).getTasks().get(0).getTaskid()));
		assertEquals(2, SubmitSolutionTest.numberOfFiles(taskPath));
		assertEquals(1, SubmitSolutionTest.numberOfFiles(taskPath.resolve(TaskPath.ADVISORFILES.getPathComponent())));
		assertEquals(1, SubmitSolutionTest.numberOfFiles(taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent())));
		final String advisorFile = Files.readString(taskPath.resolve(TaskPath.ADVISORFILES.getPathComponent() + "/HelloWorld.java"));
		assertEquals("public class HelloWorld {\r\n	public static void main(String[] args) {\r\n\r\n	}\r\n}\r\n", advisorFile);
		final String modelSolutionFile = Files.readString(taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent() + "/HelloWorld.java"));
		assertEquals("/**\r\n * Class that prints out a greeting to the console\r\n * @author Sven Strickroth\r\n * @version 2.0\r\n */\r\npublic class HelloWorld {\r\n	/**\r\n	 * Prints the greeting message\r\n	 * @param args Command line parameters, not used\r\n	 */\r\n	public static void main(String[] args) {\r\n		System.out.println(greeting());\r\n	}\r\n\r\n	/**\r\n	 * Returns the greeting to print out.\r\n	 * @return the greeting\r\n	 */\r\n	private static String greeting() {\r\n		return \"Hello World!\";\r\n	}\r\n}\r\n", modelSolutionFile);
		session.getTransaction().rollback();
	}

	@Test
	public void testImportGroupsToLecture2(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(2);
		assertEquals(0, lecture.getTaskGroups().size());
		assertEquals(0, lecture.getGroups().size());
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export Lecture 1 (WS 2020_2021) only groups.xml")) {
			final Map<Task, Set<String>> skippedFiles = new HashMap<>();
			final Lecture newLecture = LectureImportExportHelper.importLecture(session, lecture, admin, tempDir, inputStream, skippedFiles);
			assertTrue(skippedFiles.isEmpty(), "no skipped files");
			assertEquals("Lecture 2 Groupwise", newLecture.getName());
			assertEquals(0, newLecture.getTaskGroups().size());
			assertEquals(5, newLecture.getGroups().size());
		}
		assertTrue(PathUtils.isEmptyDirectory(tempDir));
		session.getTransaction().rollback();
	}

	@Test
	public void testImportLecture2ForwardCompatibility(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export Lecture 2 Groupwise (WS 2020_2021)-forward-compatibility.xml")) {
			final Map<Task, Set<String>> skippedFiles = new HashMap<>();
			final List<Lecture> lectures = DAOFactory.LectureDAOIf(session).getLectures();
			final int maxId = lectures.stream().mapToInt(l -> l.getId()).max().orElse(0);
			final Lecture newLecture = LectureImportExportHelper.importLecture(session, null, admin, tempDir, inputStream, skippedFiles);
			assertTrue(skippedFiles.isEmpty(), "no skipped files");
			assertTrue(newLecture.getId() > maxId);
			assertEquals("Lecture 2 Groupwise", newLecture.getName());
			assertEquals(0, newLecture.getTaskGroups().size());
		}
		assertTrue(PathUtils.isEmptyDirectory(tempDir));
		session.getTransaction().rollback();
	}

	@Test
	public void testImportLecture2BackwardCompatibility(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export Lecture 2 Groupwise (WS 2020_2021)-backward-compatibility.xml")) {
			final Map<Task, Set<String>> skippedFiles = new HashMap<>();
			final List<Lecture> lectures = DAOFactory.LectureDAOIf(session).getLectures();
			final int maxId = lectures.stream().mapToInt(l -> l.getId()).max().orElse(0);
			final Lecture newLecture = LectureImportExportHelper.importLecture(session, null, admin, tempDir, inputStream, skippedFiles);
			assertTrue(skippedFiles.isEmpty(), "no skipped files");
			assertTrue(newLecture.getId() > maxId);
			assertEquals("Lecture 2 Groupwise", newLecture.getName());
			assertEquals(true, newLecture.isAllowSelfSubscribe());
			assertEquals(0, newLecture.getTaskGroups().size());
		}
		assertTrue(PathUtils.isEmptyDirectory(tempDir));
		session.getTransaction().rollback();
	}

	@Test
	public void testImportBrokenSemester(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export broken.xml")) {
			assertThrows(RuntimeException.class, () -> {
				LectureImportExportHelper.importLecture(session, null, admin, tempDir, inputStream, new HashMap<>());
			});
		}
		session.getTransaction().rollback();
	}

	@Test
	public void testImportBrokenTaskType(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export broken2.xml")) {
			assertThrows(JsonMappingException.class, () -> {
				LectureImportExportHelper.importLecture(session, null, admin, tempDir, inputStream, new HashMap<>());
			});
		}
		session.getTransaction().rollback();
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "<filename />", "<filename></filename>", "<filename> </filename>" })
	public void testImportInvalidFilename(final String evilFilename, @TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		final String importfile = Files.readString(Path.of("src/test/resources/Export invalid filename.xml")).replace("<filename></filename>", evilFilename);
		assertThrows(JsonMappingException.class, () -> {
			LectureImportExportHelper.importLecture(session, null, admin, tempDir, new ByteArrayInputStream(importfile.getBytes()), new HashMap<>());
		});
		session.getTransaction().rollback();
	}

	@ParameterizedTest
	@ValueSource(strings = { "/something", "/something/../../logs/something.txt", "/something/../logs/something.txt", "../something.txt", "../something/a.txt", "logs/something.txt", "newFolder/something.txt", "junittest0.jar", "advisorfiles" })
	public void testImportEvilFilename(final String evilFilename, @TempDir final Path tempDir) throws IOException {
		final Map<Task, Set<String>> skippedFiles = new HashMap<>();
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		final String importfile = Files.readString(Path.of("src/test/resources/Export invalid filename.xml")).replace("<filename></filename>", "<filename>" + evilFilename + "</filename>");
		LectureImportExportHelper.importLecture(session, null, admin, tempDir, new ByteArrayInputStream(importfile.getBytes()), skippedFiles);
		assertFalse(skippedFiles.isEmpty(), "evil file is skipped");
		session.getTransaction().rollback();
	}

	@Test
	public void testImportJUnitTest(@TempDir final Path tempDir) throws IOException {
		session.beginTransaction();
		final User admin = DAOFactory.UserDAOIf(session).getUser(1);
		try (InputStream inputStream = new FileInputStream("src/test/resources/Export JUnit-Test.xml")) {
			final Map<Task, Set<String>> skippedFiles = new HashMap<>();
			final List<Lecture> lectures = DAOFactory.LectureDAOIf(session).getLectures();
			final int maxId = lectures.stream().mapToInt(l -> l.getId()).max().orElse(0);
			final Lecture newLecture = LectureImportExportHelper.importLecture(session, null, admin, tempDir, inputStream, skippedFiles);
			assertTrue(skippedFiles.isEmpty(), "no skipped files");
			assertTrue(newLecture.getId() > maxId);
			assertEquals("Lecture 1", newLecture.getName());
			assertEquals(1, newLecture.getTaskGroups().size());
			assertEquals(1, newLecture.getTaskGroups().get(0).getTasks().size());
			assertEquals(1, newLecture.getTaskGroups().get(0).getTasks().get(0).getTests().size());
			final Path taskPath = tempDir.resolve("lectures").resolve(String.valueOf(newLecture.getId())).resolve(String.valueOf(newLecture.getTaskGroups().get(0).getTasks().get(0).getTaskid()));
			assertTrue(Files.exists(taskPath.resolve(String.format(JavaJUnitTest.FILENAME_PATTERN, newLecture.getTaskGroups().get(0).getTasks().get(0).getTests().get(0).getId()))));
		}
		assertEquals(1, SubmitSolutionTest.numberOfFiles(tempDir));
		session.getTransaction().rollback();
	}
}
