package de.tuclausthal.abgabesystem.template;

import java.io.IOException;
import java.io.PrintWriter;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class Template {
	public void printTemplateHeader(String title) throws IOException {
		MainBetterNameHereRequired.getServletResponse().setContentType("text/html");
		MainBetterNameHereRequired.getServletResponse().setCharacterEncoding("UTF-8");
		PrintWriter out = MainBetterNameHereRequired.getServletResponse().getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" + title + "</title>");
		out.println("<link rel=StyleSheet type=\"text/css\" href=\"/ba/formate.css\">");
		out.println("</head>");
		out.println("<body><hr><h1>" + title + "</h1><hr><p>");
	}

	public void printTemplateFooter() throws IOException {
		PrintWriter out = MainBetterNameHereRequired.getServletResponse().getWriter();
		out.println("<p><hr>");
		User user = (User) MainBetterNameHereRequired.getServletRequest().getAttribute("user");
		if (user != null) {
			out.println("logged in as: " + user.getEmail());
			out.println(" - <a href=\"" + MainBetterNameHereRequired.getServletResponse().encodeURL("/ba/servlets/Overview") + "\">Übersicht</a>");
			if (user.isSuperUser()) {
				out.println(" - <a href=\"" + MainBetterNameHereRequired.getServletResponse().encodeURL("/ba/servlets/AdminMenue") + "\">Admin-Menü</a>");
			}
		} else {
			out.println("not logged in");
		}
		out.println("</body>");
		out.println("</html>");
	}
}
