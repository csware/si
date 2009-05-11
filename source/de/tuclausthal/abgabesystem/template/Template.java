package de.tuclausthal.abgabesystem.template;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestWrapper;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

/**
 * An easy template
 * @author Sven Strickroth
 */
public class Template {
	/**
	 * print a HTML page heading
	 * @param title
	 * @throws IOException
	 */
	public void printTemplateHeader(String title) throws IOException {
		MainBetterNameHereRequired.getServletResponse().setContentType("text/html");
		MainBetterNameHereRequired.getServletResponse().setCharacterEncoding("UTF-8");
		PrintWriter out = MainBetterNameHereRequired.getServletResponse().getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" + title + "</title>");
		out.println("<link rel=StyleSheet type=\"text/css\" href=\"/ba/formate.css\">");
		out.println("</head>");
		out.println("<body><img src=\"/ba/logo.gif\" alt=\"TU Clausthal\" height=\"64\" width=\"367\"><p><h1>" + title + "</h1><p>");
	}

	/**
	 * print a HTML page footer
	 * @throws IOException
	 */
	public void printTemplateFooter() throws IOException {
		PrintWriter out = MainBetterNameHereRequired.getServletResponse().getWriter();
		out.println("<p><hr>");
		User user = (User) MainBetterNameHereRequired.getServletRequest().getAttribute("user");
		if (user != null) {
			out.println("logged in as: " + user.getEmail());
			out.println(" - <a href=\"" + MainBetterNameHereRequired.getServletResponse().encodeURL("Overview") + "\">Übersicht</a>");
			if (user.isSuperUser()) {
				out.println(" - <a href=\"" + MainBetterNameHereRequired.getServletResponse().encodeURL("AdminMenue") + "\">Admin-Menü</a>");
			}
			out.println(" - <a href=\"" + MainBetterNameHereRequired.getServletResponse().encodeURL("Logout") + "\">LogOut</a>");
		} else {
			out.println("not logged in");
		}
		out.println("</body>");
		out.println("</html>");
	}
}
