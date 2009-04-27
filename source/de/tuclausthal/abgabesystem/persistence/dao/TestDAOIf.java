package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.JUnitTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.RegExpTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.Test;

public interface TestDAOIf {
	public JUnitTest createJUnitTest(Task task);

	public RegExpTest createRegExpTest(Task task);

	public void saveTest(Test test);

	public void deleteTest(Test test);
}
