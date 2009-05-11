package de.tuclausthal.abgabesystem.auth.loginimpl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.auth.LoginData;
import de.tuclausthal.abgabesystem.auth.LoginIf;

public class Form implements LoginIf {
	@Override
	public void fail_nodata(HttpServletRequest request, HttpServletResponse response) throws IOException {
		fail_nodata("", request, response);
	}

	@Override
	public void fail_nodata(String error, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO: Template!?, bzw. MainBetterNameHereRequired
		response.setContentType("text/html");
		try {
			MainBetterNameHereRequired.template().printTemplateHeader("Login erforderlich");
			PrintWriter out = response.getWriter();
			if (!error.isEmpty()) {
				out.println("<div class=\"red,mid\">" + error + "</div>");
			}
			out.print("<form action=\"");
			//out.print(response.encodeURL(MainBetterNameHereRequired.getServletRequest().getRequestURL().toString()));
			out.println("\" method=POST>");
			out.println("<table>");
			out.println("<tr>");
			out.println("<th>");
			out.println("Benutzername:");
			out.println("</td>");
			out.println("<td>");
			out.println("<input type=text size=20 name=username>");
			out.println("</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>");
			out.println("Passwort:");
			out.println("</td>");
			out.println("<td>");
			out.println("<input type=password size=20 name=password>");
			out.println("</td>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid>");
			out.println("<input type=submit value=\"Log in\">");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
			MainBetterNameHereRequired.template().printTemplateFooter();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public LoginData get_login_info(HttpServletRequest request) {
		if (request.getParameter("username") != null && !request.getParameter("username").isEmpty() && request.getParameter("password") != null && !request.getParameter("password").isEmpty()) {
			return new LoginData(request.getParameter("username"), request.getParameter("password"));
		} else {
			return null;
		}
	}

	@Override
	public boolean requires_verification() {
		return true;
	}

	@Override
	public boolean redirectAfterLogin() {
		return true;
	}
}
