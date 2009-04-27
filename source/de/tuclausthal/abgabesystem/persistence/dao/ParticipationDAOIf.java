package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.List;

import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

/**
 * Data Access Object Interface for the Participation-class
 * @author Sven Strickroth
 */
public interface ParticipationDAOIf {
	/**
	 * Creates a new Participation for a user to a lecture with a specific role
	 * @param user the user
	 * @param lecture the lecture
	 * @param type the role-type
	 * @return the (new or updated) participation
	 */
	public Participation createParticipation(User user, Lecture lecture, ParticipationRole type);

	/**
	 * Fetch a participation for a user in a specific lecture
	 * @param user the user
	 * @param lecture the lecture
	 * @return the participation or null
	 */
	public Participation getParticipation(User user, Lecture lecture);

	/**
	 * Fetch a participation by the participation id
	 * @param participationid the id
	 * @return the participation or null
	 */
	public Participation getParticipation(int participationid);

	/**
	 * Returns the participation for a lecture which are not associated with a group for a given lecture
	 * @param lecture the lecture
	 * @return participation list
	 */
	public List<Participation> getParticipationsWithoutGroup(Lecture lecture);

	/**
	 * Removes a specific participation
	 * @param participation the participation to delete
	 */
	public void deleteParticipation(Participation participation);

	/**
	 * Update/Save a given participation
	 * @param participation the participation to update
	 */
	public void saveParticipation(Participation participation);

	/**
	 * Remove a participation based on the user-lecture association
	 * @param user the user of the participation
	 * @param lecture the lecture
	 */
	public void deleteParticipation(User user, Lecture lecture);
}
