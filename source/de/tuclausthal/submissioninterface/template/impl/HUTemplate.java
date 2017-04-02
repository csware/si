package de.tuclausthal.submissioninterface.template.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;

/**
 * An template for the HU-Berlin layout
 * @author Sven Strickroth
 */
public class HUTemplate extends Template {
	public HUTemplate(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		super(servletRequest, servletResponse);
	}

	@Override
	public void printTemplateHeader(String title, String breadCrum) throws IOException {
		servletResponse.setContentType("text/html");
		servletResponse.setCharacterEncoding("iso-8859-1");
		PrintWriter out = servletResponse.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
		out.println("<html lang=\"de\">");
		out.println("<head>");
		printStyleSheets(out);
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"" + prefix + "/scripts.js\"></script>");
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
			out.println("Benutzer: " + user.getEmail());
			if (user.isSuperUser()) {
				out.println(" <span class=\"menu-divider\">|</span> <a href=\"" + servletResponse.encodeURL("AdminMenue") + "\">Admin-Menü</a>");
			}
			if (requestAdapter.isPrivacyMode()) {
				out.println(" <span class=\"menu-divider\">|</span> Privacy-Mode");
			} else if (requestAdapter.isInTUCNet()) {
				out.println(" <span class=\"menu-divider\">|</span> <a href=\"" + servletResponse.encodeURL("SwitchLogin?uid=" + user.getUid()) + "\">Tutor Login</a>");
			}
			out.println(" <span class=\"menu-divider\">|</span> <a href=\"" + servletResponse.encodeURL("Logout") + "\">LogOut</a>");
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
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + prefix + "/template/hu/default.css\">");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + prefix + "/template/hu/si.css\">");
	}
}
