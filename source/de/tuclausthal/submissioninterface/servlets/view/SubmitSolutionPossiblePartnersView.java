/*
 * Copyright 2009-2011, 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for the submission of files
 * @author Sven Strickroth
 */
public class SubmitSolutionPossiblePartnersView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");

		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		}
		Document document = builder.newDocument();
		Element rootElement = document.createElement("possiblepartners");
		document.appendChild(rootElement);

		if (task.getMaxSubmitters() > 1 && submission == null) {
			if (participation.getGroup() != null && participation.getGroup().isSubmissionGroup()) {
				Element info = document.createElement("info");
				info.setTextContent("Diese Abgabe wird automatisch für alle Studenten in Ihrer Gruppe durchgeführt.");
				rootElement.appendChild(info);
			} else if (task.isAllowSubmittersAcrossGroups() || participation.getGroup() != null) {
				Element partners = document.createElement("partners");

				Set<Participation> participations = null;
				if (task.isAllowSubmittersAcrossGroups()) {
					participations = task.getTaskGroup().getLecture().getParticipants();
				} else {
					participations = participation.getGroup().getMembers();
				}
				int cnt = 0;
				for (Participation part : participations) {
					// filter out students which already have a submission and users which are in a submissiongroup (otherwise the other participants of the submissiongroup cannot submit a solution any more)
					if (part.getId() != participation.getId() && part.getRoleType().equals(ParticipationRole.NORMAL) && (!task.isAllowSubmittersAcrossGroups() || part.getGroup() == null || !part.getGroup().isSubmissionGroup()) && submissionDAO.getSubmission(task, part.getUser()) == null) {
						Element partner = document.createElement("partner");
						partner.setTextContent(Util.escapeHTML(part.getUser().getFullName()));
						partner.setAttribute("id", String.valueOf(part.getId()));
						partners.appendChild(partner);
						cnt++;
					}
				}
				if (cnt == 0) {
					Element info = document.createElement("info");
					StringBuffer setWithUser = new StringBuffer("Sie können im Moment keinen Partner für Ihre Abgabe auswählen. Um dies zu erreichen müssen Sie die folgenden Voraussetzungen erfüllen:<ol><li>Ihr Partner muss sich auch (mindestens) einmal an diesem System angemeldet haben</li>");
					setWithUser.append("<li>Ihr Partner darf noch keine eigene Abgabe vorgenommen haben.</li>");
					if (!task.isAllowSubmittersAcrossGroups()) {
						setWithUser.append("<li>Sie, als auch Ihr Partner, müssen von Ihrem Tutor in die gleiche Übungsgruppe aufgenommen worden sein.</li>");
					} else {
						setWithUser.append("<li>Ihr Partner darf keiner Gruppe angehören, die für Gruppenabgaben konfiguriert ist.</li>");
					}
					setWithUser.append("</ol>");
					info.setTextContent(setWithUser.toString());
					rootElement.appendChild(info);
				} else {
					partners.setAttribute("maxPartners", String.valueOf(task.getMaxSubmitters() - 1));
					partners.setAttribute("info", "Haben Sie diese Aufgabe zusammen mit einem Partner gelöst? Dann bitte hier auswählen:");
					rootElement.appendChild(partners);
				}
			} else if (participation.getGroup() == null) {
				Element info = document.createElement("info");
				info.setTextContent("Sie können im Moment keinen Partner für Ihre Abgabe auswählen. Um dies zu erreichen müssen Sie zwei Voraussetzungen erfüllen:<ol><li>Ihr Partner muss sich auch (mindestens) einmal an diesem System angemeldet haben</li><li>Sie, als auch Ihr Partner, müssen von Ihrem Tutor in die gleiche Übungsgruppe aufgenommen worden sein.</li></ol>");
				rootElement.appendChild(info);
			}
		}
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(response.getOutputStream());
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
	}
}
