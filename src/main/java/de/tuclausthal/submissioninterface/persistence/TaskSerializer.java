/*
 * Copyright 2022-2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.MCOptionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.UMLConstraintTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaJUnitTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaUMLConstraintTest;
import de.tuclausthal.submissioninterface.util.TaskPath;
import de.tuclausthal.submissioninterface.util.Util;

public class TaskSerializer extends StdSerializer<Task> implements ContextualSerializer, ResolvableSerializer {
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final JsonSerializer<Object> defaultSerializer;
	private final Session session;
	private final Path lecturePath;
	private final static Pattern TEST_DRIVER_PATTERN = Pattern.compile("(" + JavaJUnitTest.FILENAME_PREFIX + "|" + JavaUMLConstraintTest.FILENAME_PREFIX + ")(\\d+)\\..+");

	public TaskSerializer(final JsonSerializer<Object> defaultSerializer, final Session session, final Path lecturePath) {
		super(Task.class);
		this.defaultSerializer = defaultSerializer;
		this.session = session;
		this.lecturePath = lecturePath;
	}

	// https://www.baeldung.com/jackson-call-default-serializer-from-custom-serializer
	@Override
	public void serialize(final Task value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeFieldName("taskConfiguration");
		defaultSerializer.serialize(value, gen, provider);

		if (value.isSCMCTask()) {
			final MCOptionDAOIf mcOptionDAO = DAOFactory.MCOptionDAOIf(session);
			final List<MCOption> options = mcOptionDAO.getMCOptionsForTask(value);
			gen.writeObjectFieldStart("scmcoptions");
			for (final MCOption mcOption : options) {
				provider.defaultSerializeField("scmcoption", mcOption, gen);
			}
			gen.writeEndObject();
		}
		final Map<String, byte[]> files = getStoredFiles(value);
		if (files != null) {
			gen.writeObjectFieldStart("files");
			for (final String file : files.keySet().stream().sorted().collect(Collectors.toList())) {
				gen.writeObjectFieldStart("file");
				gen.writeStringField("filename", file);
				gen.writeStringField("encoding", "base64");
				gen.writeBinaryField("content", files.get(file));
				gen.writeEndObject();
			}
			gen.writeEndObject();
		}

		gen.writeEndObject();
	}

	private Map<String, byte[]> getStoredFiles(final Task task) throws IOException {
		if (!Files.isDirectory(lecturePath)) {
			return null;
		}
		final Path taskPath = lecturePath.resolve(String.valueOf(task.getTaskid()));
		if (!Files.isDirectory(taskPath)) {
			return null;
		}
		final List<String> taskPaths = Stream.of(TaskPath.values()).map(tp -> tp.getPathComponent()).collect(Collectors.toList());
		final Map<String, byte[]> result = new LinkedHashMap<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(taskPath)) {
			for (final Path file : directoryStream) {
				if (Util.isInteger(file.getFileName().toString()) || file.endsWith(TaskPath.LOGS.getPathComponent())) {
					continue;
				}
				if (!taskPaths.contains(file.getFileName().toString())) {
					String relativeFilename = taskPath.relativize(file).toString();
					final Matcher m = TEST_DRIVER_PATTERN.matcher(relativeFilename);
					if (m.matches()) {
						int id = Util.parseInteger(m.group(2), -1);
						if (id <= 0) {
							continue;
						}
						if (JavaJUnitTest.FILENAME_PREFIX.equals(m.group(1)) && String.format(JavaJUnitTest.FILENAME_PATTERN, id).equals(relativeFilename)) {
							final OptionalInt indexOpt = IntStream.range(0, task.getTests().size()).filter(i -> task.getTests().get(i).getId() == id && task.getTests().get(i) instanceof JUnitTest).findFirst();
							if (indexOpt.isEmpty()) {
								continue;
							}
							relativeFilename = String.format(JavaJUnitTest.FILENAME_PATTERN, indexOpt.getAsInt());
						} else if (JavaUMLConstraintTest.FILENAME_PREFIX.equals(m.group(1)) && String.format(JavaUMLConstraintTest.FILENAME_PATTERN, id).equals(relativeFilename)) {
							final OptionalInt indexOpt = IntStream.range(0, task.getTests().size()).filter(i -> task.getTests().get(i).getId() == id && task.getTests().get(i) instanceof UMLConstraintTest).findFirst();
							if (indexOpt.isEmpty()) {
								continue;
							}
							relativeFilename = String.format(JavaUMLConstraintTest.FILENAME_PATTERN, indexOpt.getAsInt());
						} else {
							assert false;
							continue;
						}
					}
					result.put(relativeFilename, Files.readAllBytes(file));
					continue;
				}
				handleFileRecursively(taskPath, file, result);
			}
		}
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}

	private static void handleFileRecursively(final Path taskPath, final Path file, final Map<String, byte[]> result) throws IOException {
		if (Files.isDirectory(file)) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(file)) {
				for (final Path aFile : directoryStream) {
					handleFileRecursively(taskPath, aFile, result);
				}
			}
			return;
		}
		String relativeFilename = taskPath.relativize(file).toString();
		relativeFilename = relativeFilename.replace('\\', '/');
		result.put(relativeFilename, Files.readAllBytes(file));
	}

	@Override
	public void resolve(final SerializerProvider provider) throws JsonMappingException {
		((ResolvableSerializer) defaultSerializer).resolve(provider);
	}

	@Override
	public JsonSerializer<?> createContextual(final SerializerProvider prov, final BeanProperty property) throws JsonMappingException {
		return this;
	}

	public static void storeFilesToDisk(final Task task, final Path lecturePath, final Map<Task, Map<String, byte[]>> files, final Map<Task, Set<String>> skippedFiles) {
		if (files.isEmpty() || !files.containsKey(task)) {
			return;
		}
		final Path taskPath = lecturePath.resolve(String.valueOf(task.getTaskid()));
		final List<Path> taskPaths = Stream.of(TaskPath.values()).filter(tp -> tp.isAllowImportExport()).map(tp -> taskPath.resolve(tp.getPathComponent())).collect(Collectors.toList());
		try {
			Util.ensurePathExists(taskPath);
		} catch (final IOException e) {
			LOG.error("Could not create task directory", e);
			skippedFiles.put(task, files.get(task).keySet());
			return;
		}
		for (String filename : files.get(task).keySet()) {
			boolean pathException = false;
			final String key = filename;
			final Matcher m = TEST_DRIVER_PATTERN.matcher(filename);
			if (m.matches()) {
				final int index = Util.parseInteger(m.group(2), -1);
				if (index < 0 || index >= task.getTests().size()) {
					skippedFiles.computeIfAbsent(task, k -> new HashSet<>()).add(filename);
					LOG.warn("Importing XML file failed, invalid index in test driver filename");
					continue;
				}
				if (JavaJUnitTest.FILENAME_PREFIX.equals(m.group(1)) && String.format(JavaJUnitTest.FILENAME_PATTERN, index).equals(filename) && task.getTests().get(index) instanceof JUnitTest) {
					filename = String.format(JavaJUnitTest.FILENAME_PATTERN, task.getTests().get(index).getId());
					pathException = true;
				} else if (JavaUMLConstraintTest.FILENAME_PREFIX.equals(m.group(1)) && String.format(JavaUMLConstraintTest.FILENAME_PATTERN, index).equals(filename) && task.getTests().get(index) instanceof UMLConstraintTest) {
					filename = String.format(JavaUMLConstraintTest.FILENAME_PATTERN, task.getTests().get(index).getId());
					pathException = true;
				} else {
					assert false;
					skippedFiles.computeIfAbsent(task, k -> new HashSet<>()).add(filename);
					LOG.warn("Importing XML file failed, unknown test driver filename");
					continue;
				}
			}
			final Path file = Util.buildPath(taskPath, filename);
			if (file == null) {
				LOG.warn("Importing XML file failed, filename tried escape task directory: \"{}\"", filename);
				skippedFiles.computeIfAbsent(task, k -> new HashSet<>()).add(filename);
				continue;
			}
			try {
				if (taskPaths.stream().allMatch(tp -> !file.startsWith(tp) || file.equals(tp)) && !pathException) {
					skippedFiles.computeIfAbsent(task, k -> new HashSet<>()).add(filename);
					LOG.warn("Importing XML file failed, invalid filename detected: \"{}\"", filename);
					continue;
				}
				Util.ensurePathExists(file.getParent());
				Files.write(file, files.get(task).get(key));
			} catch (final IOException e) {
				skippedFiles.computeIfAbsent(task, k -> new HashSet<>()).add(filename);
				LOG.error("Could not save file into task directory", e);
				continue;
			}
		}
	}
}
