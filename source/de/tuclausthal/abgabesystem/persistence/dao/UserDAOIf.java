package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.List;

import de.tuclausthal.abgabesystem.persistence.datamodel.User;

/**
 * Data Access Object Interface for the User-class
 * @author Sven Strickroth
 */
public interface UserDAOIf {
	/**
	 * Fetches a user with a specific id from the DB
	 * @param uid the user id
	 * @return the user or null
	 */
	public User getUser(int uid);

	/**
	 * Fetch a user based on the username/email-address
	 * @param email the username/email-address of the user
	 * @return the user or null
	 */
	public User getUser(String email);

	/**
	 * Create and store a new user with the username/email-address email in the DB
	 * @param email the email-address/username of the new user
	 * @return the new user
	 */
	public User createUser(String email);

	/**
	 * Get all users from the DB
	 * @return list of users
	 */
	public List<User> getUsers();
}
