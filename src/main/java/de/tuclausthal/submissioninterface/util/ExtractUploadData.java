/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the GATE.
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

package de.tuclausthal.submissioninterface.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.TaskNumberDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry_;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * Log data extractor and converter
 * @author Sven Strickroth
 */
public class ExtractUploadData {
	/**
	 * @param args the first argument must be to path to the submissions
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {// || !new File(args[0]).isDirectory()
			System.out.println("first parameter must point to the submission directory");
			System.exit(1);
		}

		HibernateSessionHelper.getSessionFactory();
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		session.beginTransaction();

		File basePath = new File(args[0]);

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<LogEntry> criteria = builder.createQuery(LogEntry.class);
		Root<LogEntry> root = criteria.from(LogEntry.class);
		criteria.select(root);
		criteria.where(root.get(LogEntry_.action).in(LogAction.DELETE_FILE.ordinal(), LogAction.UPLOAD.ordinal(), LogAction.UPLOAD_ADMIN.ordinal()));

		TaskNumberDAOIf tndaoif = DAOFactory.TaskNumberDAOIf(session);

		for (LogEntry entry : session.createQuery(criteria).list()) {
			File lecturePath = new File(basePath, String.valueOf(entry.getTask().getTaskGroup().getLecture().getId()));
			File taskPath = new File(lecturePath, String.valueOf(entry.getTask().getTaskid()));
			File logDir = new File(taskPath, "logs");
			File logEntryDir = new File(logDir, String.valueOf(entry.getId()));
			if (entry.getUploadFilename() == null) {
				throw new RuntimeException("no filename: " + entry.getId());
			}
			if ("!mc!".equals(entry.getUploadFilename())) {
				if (!entry.getTask().isMCTask()) {
					throw new RuntimeException("not a MC task: " + entry.getId());
				}
				if (entry.getUpload() == null) {
					throw new RuntimeException("upload for mc task is null: " + entry.getId());
				}
				entry.setAdditionalData(Json.createObjectBuilder().add("mc", Json.createArrayBuilder(Stream.of(new String(entry.getUpload(), "utf-8").split("\\|\\|")).filter(part -> !part.isEmpty()).collect(Collectors.toList()))).build().toString());
				session.save(entry);
			} else if ("!textsolution!".equals(entry.getUploadFilename())) {
				if (entry.getUpload() == null) {
					throw new RuntimeException("upload for textsolution task is null: " + entry.getId());
				}
				String content = new String(entry.getUpload(), "utf-8");
				if (entry.getTask().isADynamicTask()) {
					String parts[] = content.split("\n", 2);
					if (parts.length != 2) {
						throw new RuntimeException("FUCK0: " + entry.getId());
					}
					if (parts[1].contains("||")) {
						throw new RuntimeException("FUCK: " + entry.getId());
					}
					content = parts[1];
					String userResponseParts[] = parts[0].split("\\|\\|");
					if (userResponseParts.length == 0 && "||".equals(parts[0])) {
						userResponseParts = new String[] {""};
					}
					List<TaskNumber> taskNumbers = tndaoif.getTaskNumbersForTaskLocked(entry.getTask(), DAOFactory.ParticipationDAOIf(session).getParticipation(entry.getUser(), entry.getTask().getTaskGroup().getLecture()));
					if (taskNumbers.size() != userResponseParts.length) {
						throw new RuntimeException("Numbers mismatch: " + entry.getId());
					}
					JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
					JsonArrayBuilder taskNumbersArrayBuilder = Json.createArrayBuilder();
					for (TaskNumber tasknumber : taskNumbers) {
						JsonObjectBuilder taskNumberBuilder = Json.createObjectBuilder();
						taskNumberBuilder.add("number", tasknumber.getNumber());
						taskNumberBuilder.add("origNumber", tasknumber.getOrigNumber());
						taskNumbersArrayBuilder.add(taskNumberBuilder);
					}
					jsonBuilder.add("taskNumbers", taskNumbersArrayBuilder);
					jsonBuilder.add("userResponses", Json.createArrayBuilder(Stream.of(userResponseParts).filter(part -> !part.isEmpty()).collect(Collectors.toList())).build());
					entry.setAdditionalData(jsonBuilder.build().toString());
					session.save(entry);
				}
				logEntryDir.mkdirs();
				File writeFile = new File(logEntryDir, "textloesung.txt");
				try (FileWriter fw = new FileWriter(writeFile)) {
					fw.write(content);
				}
			} else {
				if (entry.getUploadFilename().contains("..")) {
					throw new RuntimeException("ERROR OUT: " + entry.getId());
				}
				entry.setAdditionalData(Json.createObjectBuilder().add("filename", entry.getUploadFilename()).build().toString());
				session.save(entry);
				if (entry.getAction() != LogAction.DELETE_FILE.ordinal()) {
					if (entry.getUpload() == null) {
						throw new RuntimeException("upload for is null: " + entry.getId());
					}
					logEntryDir.mkdirs();
					File writeFile = new File(logEntryDir, entry.getUploadFilename());
					try (FileOutputStream fw = new FileOutputStream(writeFile)) {
						fw.write(entry.getUpload());
					}
				}
			}
		}

		session.getTransaction().commit();
		session.close();
		HibernateSessionHelper.getSessionFactory().close();
		AbandonedConnectionCleanupThread.uncheckedShutdown();
	}
}
