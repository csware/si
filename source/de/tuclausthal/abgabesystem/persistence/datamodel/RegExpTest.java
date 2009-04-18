package de.tuclausthal.abgabesystem.persistence.datamodel;

import javax.persistence.Entity;

@Entity
public class RegExpTest extends Test {
	private String commandline;

	/**
	 * @return the commandline
	 */
	public String getCommandline() {
		return commandline;
	}

	/**
	 * @param commandline the commandline to set
	 */
	public void setCommandline(String commandline) {
		this.commandline = commandline;
	}

	/**
	 * @return the regularExpression
	 */
	public String getRegularExpression() {
		return regularExpression;
	}

	/**
	 * @param regularExpression the regularExpression to set
	 */
	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}

	private String regularExpression;
}
