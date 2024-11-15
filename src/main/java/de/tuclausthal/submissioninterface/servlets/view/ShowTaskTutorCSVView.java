/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
@GATEView
public class ShowTaskTutorCSVView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Task task = (Task) request.getAttribute("task");

		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=export.csv");

		final String[] empty = new String[0];
		final String[] fixedHeader = { "E-Mail-Adresse", "Interner Kommentar", "Externer Kommentar", "Abgenommen (0=nicht abgenommen|1=abgenommen|2=nicht bestanden)" };
		try (CSVWriter writer = new CSVWriter(new PrintWriter(response.getWriter()), ';', ICSVWriter.DEFAULT_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END)) {
			List<String> header = new ArrayList<>(Arrays.asList(fixedHeader));
			if (task.getPointCategories().isEmpty()) {
				header.add("Punkte");
			} else {
				for (PointCategory category : task.getPointCategories()) {
					if (category.isOptional()) {
						header.add(category.getDescription() + " (optional)");
					} else {
						header.add(category.getDescription());
					}
				}
			}
			writer.writeNext(header.toArray(empty), false);

			PointGivenDAOIf pointGivenDAO = DAOFactory.PointGivenDAOIf(session);
			for (Submission submission : DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task, true)) {
				String[] line = new String[header.size()];
				if (submission.getPoints() == null) {
					for (int i = 1; i < header.size(); ++i) {
						line[i] = "";
					}
				} else {
					line[1] = getValueOrEmptyString(submission.getPoints().getInternalComment());
					line[2] = getValueOrEmptyString(submission.getPoints().getPublicComment());
					switch (submission.getPoints().getTypedPointStatus()) {
						case NICHT_ABGENOMMEN:
							line[3] = "0";
							break;
						case ABGENOMMEN:
							line[3] = "1";
							break;
						case ABGENOMMEN_FAILED:
							line[3] = "2";
							break;
						case NICHT_BEWERTET:
							line[3] = "";
							break;
					}
					if (task.getPointCategories().isEmpty()) {
						line[fixedHeader.length] = Util.showPoints(submission.getPoints().getPoints());
					} else {
						Map<Integer, PointGiven> pointsGiven = pointGivenDAO.getPointsGivenOfSubmission(submission).stream().collect(Collectors.toMap(p -> p.getCategory().getPointcatid(), p -> p));
						int i = fixedHeader.length;
						for (PointCategory category : task.getPointCategories()) {
							PointGiven pointGiven = pointsGiven.get(category.getPointcatid());
							if (pointGiven != null) {
								line[i] = Util.showPoints(pointGiven.getPoints());
							} else {
								line[i] = "0";
							}
							++i;
						}
					}
				}
				for (Participation part : submission.getSubmitters()) {
					line[0] = part.getUser().getEmail();
					writer.writeNext(line, false);
				}
			}
			writer.flush();
		}
	}

	public static String getValueOrEmptyString(String value) {
		return value == null ? "" : value;
	}
}
