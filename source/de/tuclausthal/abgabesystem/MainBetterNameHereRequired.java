package de.tuclausthal.abgabesystem;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import de.tuclausthal.abgabesystem.auth.Auth;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;
import de.tuclausthal.abgabesystem.template.Template;

/**
 * Singleton+Facade
 * @author Sven Strickroth
 *
 */
public class MainBetterNameHereRequired {
	private static ThreadLocal<MainBetterNameHereRequired> instance = new ThreadLocal<MainBetterNameHereRequired>();

	/**
	 * @return the servletRequest
	 */
	public static HttpServletRequest getServletRequest() {
		return instance.get().servletRequest;
	}

	/**
	 * @return the servletResponse
	 */
	public static HttpServletResponse getServletResponse() {
		return instance.get().servletResponse;
	}

	public static Template template() {
		return new Template();
	}

	private HttpServletRequest servletRequest;

	private HttpServletResponse servletResponse;
	private final SessionFactory sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();;
	private Session session;

	/**
	 * Gibt eine Hibernation-Datenbank-Sitzung zurück
	 * @return
	 * @throws HibernateException
	 */
	public static Session getSession() throws HibernateException {
		return instance.get().session;
	}

	public MainBetterNameHereRequired(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		instance.set(this);

		// set up db connection
		try {
			session = sessionFactory.openSession();
		} catch (Throwable ex) {
			// Log exception!
			throw new ExceptionInInitializerError(ex);
		}

		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
	}

	public User getUser() {
		return (User) servletRequest.getAttribute("user");
	}

	public void login() throws IOException {
		Auth auth = new Auth();
		auth.login();
	}
}
