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

package de.tuclausthal.submissioninterface.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Entity;

import org.hibernate.Session;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.tuclausthal.submissioninterface.persistence.TaskDescriptionSerializer;
import de.tuclausthal.submissioninterface.persistence.TaskDeserializerModifier;
import de.tuclausthal.submissioninterface.persistence.TaskSerializer;
import de.tuclausthal.submissioninterface.persistence.TaskSerializerModifier;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.PointCategoryDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;

public class LectureImportExportHelper {
	public static void exportLecture(final Session session, final Lecture lecture, final Path lecturePath, final OutputStream outputStream) throws IOException {
		final XmlMapper xmlMapper = XmlMapper.builder().enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS).build();
		xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
		final SimpleModule module = new SimpleModule();
		module.setSerializerModifier(new TaskSerializerModifier(session, lecturePath));
		xmlMapper.registerModule(module);
		xmlMapper.registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		xmlMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, lecture);
	}

	public static Lecture importLecture(final Session session, final Lecture importToLecture, final User user, final Path dataPath, final InputStream inputStream, final Map<Task, Set<String>> skippedFiles) throws IOException {
		final XmlMapper xmlMapper = XmlMapper.builder().enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS).build();
		xmlMapper.enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL);
		xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		xmlMapper.registerModule(new JavaTimeModule()).disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		// https://www.baeldung.com/jackson-deserialization
		xmlMapper.registerSubtypes(new Reflections("de.tuclausthal.submissioninterface.persistence.datamodel").getTypesAnnotatedWith(Entity.class));
		final SimpleModule module = new SimpleModule();
		final Map<Task, Map<String, byte[]>> files = new HashMap<>();
		final List<MCOption> mcOptions = new ArrayList<>();
		module.setDeserializerModifier(new TaskDeserializerModifier(files, mcOptions));
		xmlMapper.registerModule(module);
		final Lecture parsedLecture = xmlMapper.readValue(inputStream, Lecture.class);

		if (parsedLecture.getTaskGroups() == null) {
			parsedLecture.setTaskGroups(Collections.emptyList());
		}
		parsedLecture.getTaskGroups().stream().forEach(tg -> {
			if (tg.getTitle() == null || tg.getTitle().isBlank()) {
				throw new RuntimeException("Invalid taskgroup title");
			}
			if (tg.getTasks() == null) {
				tg.setTasks(Collections.emptyList());
			}
		});
		parsedLecture.getTaskGroups().stream().flatMap(tg -> tg.getTasks().stream()).forEach(task -> {
			if (task.getTitle() == null || task.getTitle().isBlank()) {
				throw new RuntimeException("Invalid task title");
			}
			if (task.getDeadline().isBefore(task.getStart())) {
				task.setDeadline(task.getStart());
			}
			if (task.getShowPoints() != null && task.getShowPoints().isBefore(task.getDeadline())) {
				task.setShowPoints(task.getDeadline());
			}
			if (task.isSCMCTask() || task.isClozeTask()) {
				task.setFilenameRegexp("-");
				task.setShowTextArea("-"); // be explicit here, it's disabled by default
				task.setDynamicTask(null);
				task.setTutorsCanUploadFiles(false);
			} else if (task.isADynamicTask()) {
				task.setFilenameRegexp("-");
				task.setShowTextArea("textloesung.txt");
				task.setTutorsCanUploadFiles(false);
			} else {
				task.setDynamicTask(null);
			}
			if (task.isClozeTask()) {
				final ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), null, false, false);
				final int points = clozeHelper.maxPoints();
				if (points > task.getMaxPoints()) {
					task.setMaxPoints(points);
				}
			}
			if (task.getPointCategories() == null) {
				task.setPointCategories(new ArrayList<>());
			}
			task.getPointCategories().stream().forEach(pointCategory -> pointCategory.setPoints(Util.ensureMinPointStepMultiples(pointCategory.getPoints(), task.getMinPointStep())));
			if (task.getTests() == null) {
				task.setTests(Collections.emptyList());
			}
			task.getTests().stream().forEach(test -> {
				if (test.getTestTitle() == null || test.getTestTitle().isBlank()) {
					throw new RuntimeException("Invalid task title");
				}
			});
			if (task.getSimilarityTests() == null) {
				task.setSimilarityTests(Collections.emptyList());
			}
			task.getSimilarityTests().stream().forEach(similarityTest -> {
				if (similarityTest.getDupeCheck(null) == null) {
					throw new RuntimeException("Unknown similarity check");
				}
			});
		});
		if (parsedLecture.getGroups() == null) {
			parsedLecture.setGroups(Collections.emptyList());
		}

		if (importToLecture == null) {
			if (parsedLecture.getName() == null || parsedLecture.getName().isBlank()) {
				throw new RuntimeException("Invalid lecture title");
			}
			if (parsedLecture.getSemester() < 20000 || parsedLecture.getSemester() > (ZonedDateTime.now().getYear() + 2) * 10 || (parsedLecture.getSemester() % 10 != 0 && parsedLecture.getSemester() % 10 != 1)) {
				throw new RuntimeException("Semester invalid");
			}
			session.persist(parsedLecture);
			DAOFactory.ParticipationDAOIf(session).createParticipation(user, parsedLecture, ParticipationRole.ADVISOR);
		} else {
			parsedLecture.getTaskGroups().stream().forEach(tg -> {
				tg.setLecture(importToLecture);
				importToLecture.getTaskGroups().add(tg);
				session.persist(tg);
			});
			parsedLecture.getGroups().stream().forEach(group -> {
				group.setLecture(importToLecture);
				importToLecture.getGroups().add(group);
				session.persist(group);
			});
		}
		for (MCOption mcOption : mcOptions) {
			session.persist(mcOption);
		}
		parsedLecture.getTaskGroups().stream().flatMap(tg -> tg.getTasks().stream()).forEach(task -> {
			task.setDescription(task.getDescription().replaceAll(TaskDescriptionSerializer.TASKID_PLACEHOLDER, "taskid=" + String.valueOf(task.getTaskid())));
		});
		final PointCategoryDAOIf pointCategoryDAO = DAOFactory.PointCategoryDAOIf(session);
		parsedLecture.getTaskGroups().stream().flatMap(tg -> tg.getTasks().stream()).forEach(task -> {
			if (task.getPointCategories() != null && !task.getPointCategories().isEmpty()) {
				task.setMaxPoints(pointCategoryDAO.countPoints(task));
			}
		});
		session.flush();
		if (dataPath != null) {
			final Path lecturePath;
			if (importToLecture == null) {
				lecturePath = Util.constructPath(dataPath, parsedLecture);
			} else {
				lecturePath = Util.constructPath(dataPath, importToLecture);
			}
			parsedLecture.getTaskGroups().stream().flatMap(tg -> tg.getTasks().stream()).forEach(task -> TaskSerializer.storeFilesToDisk(task, lecturePath, files, skippedFiles));
		}
		if (importToLecture == null) {
			return parsedLecture;
		}
		return importToLecture;
	}
}
