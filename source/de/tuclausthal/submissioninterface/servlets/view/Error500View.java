package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;

import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Servlet implementation class Error500
 */
public class Error500View extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		Template template = null;
		try {
			template = TemplateFactory.getTemplate(request, response);
		} catch (Exception e) {
		}
		if (template == null) {
			response.setContentType("text/html");
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
			out.println("<html><head><title>Internal Server Error (500)</title></head><body><h1>Internal Server Error (500)</h1>");
		} else {
			template.printTemplateHeader("Internal Server Error (500)");
		}
		Throwable throwable = (Throwable) request.getAttribute(Globals.EXCEPTION_ATTR);

		out.println("Das Skript, auf das Sie versuchen zuzugreifen, hat einen schweren Fehler verursacht (" + Util.mknohtml(throwable.toString()) + ").<br>");

		out.println("<br>");
		out.println("<b>Sollte dieser Fehler öfter auftreten, wenden Sie sich bitte mit der o.g. Fehlermeldung, der Adresse und Informationen, was Sie gerade versucht haben durchzuführen, an den <a href=\"mailto:" + new ContextAdapter(getServletContext()).getAdminMail() + "\">Webmaster</a>.</b><br>");

		if (template != null) {
			template.printTemplateFooter();
		} else {
			out.println("</body></html>");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
