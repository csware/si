/*
 * Copyright 2011-2012, 2017 Sven Strickroth <email@cs-ware.de>
 * Copyright 2011 Joachim Schramm
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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * View-Servlet für den ArgoUML Webstart
 * Es wird eine dynamische jnlp Datei erzeugt
 * @author Sven Strickroth
 * @author Joachim Schramm
 */
public class WebStartArgoUMLView extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/x-java-jnlp-file");

		PrintWriter out = response.getWriter();

		Task task = (Task) request.getAttribute("task");
		String sessionID = request.getSession().getId();

		String urlprefix = "http";
		if (request.isSecure()) {
			urlprefix += "s";
		}
		urlprefix += "://" + request.getServerName();
		if ((request.isSecure() && request.getServerPort() != 443) || (!request.isSecure() && request.getServerPort() != 80)) {
			urlprefix += ":" + request.getServerPort();
		}
		String servletPath = urlprefix + request.getContextPath() + "/servlets";
		urlprefix += request.getContextPath() + "/argouml/";

		//Generiere jnlp Datei
		//Statischer Teil
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		out.println("<jnlp");
		out.println("  spec=\"1.0+\"");
		out.println("  codebase=\"" + urlprefix + "\"");
		out.println("  >");
		out.println("  <information>");
		out.println("    <title>ArgoUML Latest Stable Release 0.32.2 for GATE</title>");
		out.println("    <vendor>Tigris.org (Open Source)</vendor>");
		out.println("    <homepage href=\"http://argouml.tigris.org/\"/>");
		out.println("    <description>ArgoUML application.");
		out.println("                 This is the latest stable release.");
		out.println("    </description>");
		out.println("    <description kind=\"short\">ArgoUML 0.32.2</description>");
		out.println("    <icon href=\"http://argouml.tigris.org/images/argologo16x16.gif\" width=\"16\" height=\"16\" />");
		out.println("    <icon href=\"http://argouml.tigris.org/images/argologo32x32.gif\" width=\"32\" height=\"32\" />");
		out.println("    <icon href=\"http://argouml.tigris.org/images/argologo64x64.gif\" width=\"64\" height=\"64\" />");
		out.println("  </information>");
		out.println("  <security>");
		out.println("    <all-permissions/>");
		out.println("  </security>");
		out.println("  <resources>");
		out.println("    <j2se version=\"1.5+\" max-heap-size=\"512m\"/>");
		out.println("");
		//ArgoUML Jars
		out.println("    <jar href=\"" + urlprefix + "log4j-1.2.6.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "mdrapi.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "argouml.jar\" main=\"true\"/>");
		out.println("    <jar href=\"" + urlprefix + "mof.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "nbmdr.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "ocl-argo-1.1.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "openide-util.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.common_2.6.0.v20100914-1218.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.ecore.change.edit_2.5.0.v20100521-1846.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.ecore.change_2.5.1.v20100907-1643.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.ecore.edit_2.6.0.v20100914-1218.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.ecore.xmi_2.5.0.v20100521-1846.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.ecore_2.6.1.v20100914-1218.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.edit_2.6.0.v20100914-1218.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.mapping.ecore_2.6.0.v20100914-1218.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.mapping.ecore2xml_2.5.0.v20100521-1847.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.emf.mapping_2.6.0.v20100914-1218.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.uml2.common.edit_1.5.0.v201005031530.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.uml2.common_1.5.0.v201005031530.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.uml2.uml.edit_3.1.0.v201005031530.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.uml2.uml.resources_3.1.1.v201008191505.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "org.eclipse.uml2.uml_3.1.1.v201008191505.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "swidgets-0.1.4.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "toolbar-1.4.1-20071227.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "antlr-2.7.7.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "argouml-euml.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "argouml-mdr.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "argouml-model.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "commons-logging-1.0.2.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "gef-0.13.3.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "java-interfaces.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "jmi.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "jmiutils.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "argouml-umlpropertypanels.jar\"/>");
		//Jars für den Http upload
		out.println("    <jar href=\"" + urlprefix + "commons-codec-1.4.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "commons-fileupload-1.2.2.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "commons-logging-1.1.1.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "httpclient-4.1.1.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "httpclient-cache-4.1.1.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "httpcore-4.1.jar\"/>");
		out.println("    <jar href=\"" + urlprefix + "httpmime-4.1.1.jar\"/>");
		out.println("");
		out.println("    <property name=\"argouml.modules\"");
		out.println("      value=\";org.argouml.state2.StateDiagramModule;org.argouml.sequence2.SequenceDiagramModule;org.argouml.activity2.ActivityDiagramModule;org.argouml.core.propertypanels.module.XmlPropertyPanelsModule;org.argouml.transformer.TransformerModule;org.argouml.language.cpp.generator.ModuleCpp;org.argouml.language.cpp.notation.NotationModuleCpp;org.argouml.language.cpp.profile.ProfileModule;org.argouml.language.cpp.reveng.CppImport;org.argouml.language.cpp.ui.SettingsTabCpp;org.argouml.language.csharp.generator.GeneratorCSharp;org.argouml.language.java.cognitive.critics.InitJavaCritics;org.argouml.language.java.generator.GeneratorJava;org.argouml.language.java.profile.ProfileJava;org.argouml.language.java.reveng.JavaImport;org.argouml.language.java.reveng.classfile.ClassfileImport;org.argouml.language.java.ui.JavaTools;org.argouml.language.java.ui.SettingsTabJava;org.argouml.language.php.generator.ModulePHP4;org.argouml.language.php.generator.ModulePHP5;org.argouml.language.sql.SqlInit;org.argouml.uml.reveng.classfile.ClassfileImport;org.argouml.uml.reveng.idl.IDLFileImport\"    />");
		out.println("  </resources>");
		out.println("  <application-desc main-class=\"org.argouml.application.Main\">");

		//Parameter, dynamischer Teil
		out.println("<argument>-sessionid</argument>");
		out.println("<argument>" + sessionID + "</argument>");
		out.println("<argument>-srvpath</argument>");
		out.println("<argument>" + servletPath + "</argument>");
		out.println("<argument>-taskid</argument>");
		out.println("<argument>" + task.getTaskid() + "</argument>");
		out.println("</application-desc>");
		out.println("</jnlp>");
	}
}
