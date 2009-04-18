package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.List;

import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public interface UserDAOIf {
	public User getUser(int uid);

	public User getUser(String email);

	public User createUser(String email);

	public List<User> getUsers();
}
