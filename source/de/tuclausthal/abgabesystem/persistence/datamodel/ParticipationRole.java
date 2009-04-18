package de.tuclausthal.abgabesystem.persistence.datamodel;

public enum ParticipationRole {
	NORMAL("normal"), TUTOR("tutor"), ADVISOR("advisor");

	private String roleString;

	private ParticipationRole(String roleString) {
		this.roleString = roleString;
	}
}
