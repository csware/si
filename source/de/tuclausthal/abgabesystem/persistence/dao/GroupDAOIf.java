package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Group;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;

public interface GroupDAOIf {
	public Group createGroup(Lecture lecture, String name);

	public Group getGroup(int groupid);

	public void deleteGroup(Group group);
}
