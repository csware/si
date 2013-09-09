package de.tuclausthal.submissioninterface.template.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;

/**
 * An template for the TU-Clausthal layout
 * @author Sven Strickroth
 */
public class TUCTemplate extends Template {
	public TUCTemplate(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		super(servletRequest, servletResponse);
	}

	@Override
	public void printTemplateHeader(String title, String breadCrum) throws IOException {
		servletResponse.setContentType("text/html");
		servletResponse.setCharacterEncoding("iso-8859-1");
		PrintWriter out = servletResponse.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
		out.println("<html>");
		out.println("<head>");
		printStyleSheets(out);
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"" + prefix + "/scripts.js\"></script>");
		out.println("<title>GATE: " + title + "</title>");
		getHeads();
		out.println("</head>");
		out.println("<body>");
		out.println("<div id=\"aussen\">");
		out.println("<div id=\"logo\">");
		out.println("<h1><a href=\"http://www.tu-clausthal.de/\"><img src=\"" + prefix + "/template/tuc/tuc2005.png\" width=\"344\" height=\"64\" alt=\"TU Clausthal\" border=\"0\" /></a></h1>");
		out.println("</div>");
		out.println("<div id=\"menu\">");
		User user = requestAdapter.getUser();
		if (user != null) {
			out.println("Benutzer: " + user.getEmail());
			if (user.isSuperUser()) {
				out.println(" - <a href=\"" + servletResponse.encodeURL("AdminMenue") + "\">Admin-Menü</a>");
			}
			if (requestAdapter.isPrivacyMode()) {
				out.println(" - Privacy-Mode");
			} else if (requestAdapter.isInTUCNet()) {
				out.println(" - <a href=\"" + servletResponse.encodeURL("SwitchLogin?uid=" + user.getUid()) + "\">Tutor Login</a>");
			}
			out.println(" - <a href=\"" + servletResponse.encodeURL("Logout") + "\">LogOut</a>");
		} else {
			out.println("nicht eingeloggt");
		}
		//out.println("<a href=\"http://search.tu-clausthal.de/\" target=\"_blank\">Suche</a>");
		out.println("</div>");
		out.println("<hr class=\"hide\" />");
		out.println("<div id=\"page\">");
		out.println("<div id=\"einrichtung\">");
		out.println("<h2><a href=\"http://www.in.tu-clausthal.de/\">Institut für Informatik</a></h2>");
		out.println("</div>");
		out.println("<hr class=\"hide\" />");
		out.println("<div id=\"pfad-wide\">");
		out.println(breadCrum);
		out.println("</div>");
		out.println("<hr class=\"hide\" />");
		out.println("<div id=\"inhalt-wide\">");
		out.println("<h1>" + title + "</h1>");
	}

	@Override
	public void printTemplateFooter() throws IOException {
		PrintWriter out = servletResponse.getWriter();
		out.println("<div style=\"clear: both;\">");
		out.println("</div>");
		out.println("</div>");
		out.println("<hr class=\"hide\" />");
		out.println("<div id=\"fuss-wide\">");
		out.println("Layout &copy;&nbsp;TU&nbsp;Clausthal&nbsp;2006&nbsp;&middot;&nbsp;<a href=\"http://www.tu-clausthal.de/info/impressum/\" target=\"_blank\">Impressum</a>");
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");
	}

	@Override
	public void printStyleSheets(PrintWriter out) {
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + prefix + "/template/tuc/2005/screen.css\" media=\"screen\">");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + prefix + "/template/tuc/si.css\">");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + prefix + "/template/tuc/2005/print.css\" media=\"print\">");
	}
}
