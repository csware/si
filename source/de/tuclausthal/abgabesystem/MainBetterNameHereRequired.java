package de.tuclausthal.abgabesystem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

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
	private static final SessionFactory sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();;

	private static Session session = sessionFactory.openSession();
	
	/**
	 * Gibt eine Hibernation-Datenbank-Sitzung zurück
	 * @return
	 * @throws HibernateException
	 */
	public static Session getSession() throws HibernateException {
		return session;
	}

	public MainBetterNameHereRequired(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		instance.set(this);

		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
	}

	public User getUser() {
		return (User) servletRequest.getAttribute("user");
	}
}
