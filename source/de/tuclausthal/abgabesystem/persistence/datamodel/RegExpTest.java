package de.tuclausthal.abgabesystem.persistence.datamodel;

import javax.persistence.Entity;

@Entity
public class RegExpTest extends Test {
	private String commandLineParameter;
	private String mainClass;

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

	/**
	 * @return the mainClass
	 */
	public String getMainClass() {
		return mainClass;
	}

	/**
	 * @param mainClass the mainClass to set
	 */
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	/**
	 * @return the commandLineParameter
	 */
	public String getCommandLineParameter() {
		return commandLineParameter;
	}

	/**
	 * @param commandLineParameter the commandLineParameter to set
	 */
	public void setCommandLineParameter(String commandLineParameter) {
		this.commandLineParameter = commandLineParameter;
	}
}
