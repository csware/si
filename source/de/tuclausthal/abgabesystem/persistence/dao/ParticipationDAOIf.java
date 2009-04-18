package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.List;

import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public interface ParticipationDAOIf {
	public Participation createParticipation(User user, Lecture lecture, ParticipationRole type);

	public Participation getParticipation(User user, Lecture lecture);
	
	public List<Participation> getParticipationsWithoutGroup(Lecture lecture);

	public void deleteParticipation(Participation participation);

	public void deleteParticipation(User user, Lecture lecture);
}
