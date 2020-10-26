﻿<%@page language="java" import="de.tuclausthal.submissioninterface.template.Template,de.tuclausthal.submissioninterface.template.TemplateFactory" %>
<% String title = "Willkommen bei GATE!"; %>
<% Template template = TemplateFactory.getTemplate(request, response); %>
<% template.printTemplateHeader(title, "<a href=\"" + response.encodeURL("servlets/Overview") + "\">GATE Übersicht/Login</a>"); %>

<p>GATE ist ein E-Assessment-System, das zur Unterstützung des Übungsbetriebs von Programmier-Lehrveranstaltungen entwickelt wurde.</p>

<p>Das System kann zum einen Studierende bei der Einreichung von Lösungen durch Self-Assessments zur Überprüfung der Korrektheit unterstützen und zum anderen erleichtert es TutorInnen die Feedbackgabe sowie die Bewertung von studentischen Lösungen.</p>

<strong>⇒ <a href="servlets/Overview">GATE Übersicht/Login</a></strong>

<% out.flush(); %>
<% template.printTemplateFooter(); %>
