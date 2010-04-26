package de.tuclausthal.submissioninterface.template.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * An template for the TU-Clausthal layout
 * @author Sven Strickroth
 */
public class TUCTemplate2 extends Template {

	public TUCTemplate2(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		super(servletRequest, servletResponse);
	}

	@Override
	public void printTemplateHeader(String title, String breadCrum) throws IOException {
		servletResponse.setContentType("text/html");
		servletResponse.setCharacterEncoding("UTF-8");
		PrintWriter out = servletResponse.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" + title + "</title>");
		out.println("<link rel=StyleSheet type=\"text/css\" href=\""+prefix+"/formate.css\">");
		getHeads();
		out.println("</head>");
		out.println("<body><img src=\""+prefix+"/logo.gif\" alt=\"TU Clausthal\" height=\"64\" width=\"367\"><p><h1>" + title + "</h1><p>");
	}

	@Override
	public void printTemplateFooter() throws IOException {
		PrintWriter out = servletResponse.getWriter();
		out.println("<p><hr>");
		User user = sessionAdapter.getUser(HibernateSessionHelper.getSession());
		if (user != null) {
			out.println("logged in as: " + user.getEmail());
			out.println(" - <a href=\"" + servletResponse.encodeURL("Overview") + "\">Übersicht</a>");
			if (user.isSuperUser()) {
				out.println(" - <a href=\"" + servletResponse.encodeURL("AdminMenue") + "\">Admin-Menü</a>");
			}
			out.println(" - <a href=\"" + servletResponse.encodeURL("Logout") + "\">LogOut</a>");
		} else {
			out.println("not logged in");
		}
		out.println("</body>");
		out.println("</html>");
	}
}
