package de.tuclausthal.submissioninterface.template.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * An template for the HU-Berlin layout
 * @author Sven Strickroth
 */
public class HUTemplate extends Template {
	public HUTemplate(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		super(servletRequest, servletResponse);
	}

	@SuppressWarnings("unused")
	@Override
	public void printTemplateHeader(String title, String breadCrum) throws IOException {
		servletResponse.setContentType("text/html");
		servletResponse.setCharacterEncoding("UTF-8");
		PrintWriter out = servletResponse.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
		out.println("<html lang=\"de\">");
		out.println("<head>");
		printStyleSheets(out);
		out.println("<title>GATE: " + title + "</title>");
		getHeads();
		out.println("</head>");
		out.println("<body>");
		out.println("<div id=\"all\">");
		if (false) { // menu of the left
			out.println("<div id=\"left\">");
			out.println("<div id=\"department\"><a href=\"http://informatik.hu-berlin.de\">Department of Informatics</a></div>");
			out.println("<div id=\"menu\"></div>");
			out.println("</div>");
			out.println("<div id=\"main\">");
		}
		else
		{
			out.println("<div id=\"main\" style=\"width:100%\">");
		}
		out.println("<div id=\"top\">");
		out.println("<div id=\"picture\"><a href=\"http://www.hu-berlin.de\"><img src=\"" + prefix + "/template/hu/hu-mainbuilding.jpg\" alt=\"\" width=\"360\" height=\"110\" border=\"0\"></a></div>");
		out.println("<div id=\"logo\"><a href=\"http://www.hu-berlin.de\"><img src=\"" + prefix + "/template/hu/logo.jpg\" alt=\"Humbold-Universität zu Berlin\" width=\"450\" height=\"90\" border=\"0\"></a></div>");
		out.println("<div id=\"location\">");
		User user = requestAdapter.getUser();
		if (user != null) {
			out.println("Angemeldet als: " + Util.escapeHTML(user.getUsername()));
			if (user.isSuperUser()) {
				out.println(" <span class=\"menu-divider\">|</span> <a href=\"" + Util.generateHTMLLink(prefix + "/" + Configuration.getInstance().getServletsPath() + "/AdminMenue", servletResponse) + "\">Admin-Menü</a>");
			}
			if (requestAdapter.isPrivacyMode()) {
				out.println(" <span class=\"menu-divider\">|</span> Privacy-Mode");
			} else if (requestAdapter.isIntranet()) {
				out.println(" <span class=\"menu-divider\">|</span> <a href=\"" + Util.generateHTMLLink(prefix + "/" + Configuration.getInstance().getServletsPath() + "/SwitchLogin?uid=" + user.getUid(), servletResponse) + "\">Tutor Login</a>");
			}
			out.println(" <span class=\"menu-divider\">|</span> <a href=\"" + Util.generateHTMLLink(prefix + "/" + Configuration.getInstance().getServletsPath() + "/Logout", servletResponse) + "\">LogOut</a>");
		} else {
			out.println("nicht eingeloggt");
		}
		out.println("</div>");
		out.println("<div id=\"navigation\">GATE");
		//Computer Science Education <span class="menu-divider">|</span> Computer Science and Society 
		out.println("</div>");
		out.println("</div>");
		out.println("<div style=\"clear:both;\"></div>");
		out.println("<div id=\"content\">");
		out.println("<div id=\"breadcrumbs\">" + breadCrum + "</div>");
		out.println("<h2>" + title + "</h2>");
	}

	@Override
	public void printTemplateFooter() throws IOException {
		PrintWriter out = servletResponse.getWriter();
		out.println("</div>");
		out.println("</div>");
		out.println("<div style=\"clear: both;\"></div>");
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");
	}

	@Override
	public void printStyleSheets(PrintWriter out) {
		out.println("<link rel=\"stylesheet\" href=\"" + prefix + "/template/hu/default.css\">");
		out.println("<link rel=\"stylesheet\" href=\"" + prefix + "/template/hu/si.css\">");
	}
}
