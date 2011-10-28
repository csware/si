/*
 * Copyright 2011 Giselle Rodriguez
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.servlets.controller;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.TaskNumberDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

public class RandomNumber {
	private TaskNumberDAOIf taskNumberDAO;
	private List<TaskNumber> taskNumbers;
	private int taskid;
	private int userid;
	private int submissionid;
	private String description;
	private final char[] types = { 'B', 'O', 'D', 'F', 'H', 'L' };
	private final int[][] randomParam = { { 2, 2, 0 }, { 8, 2, 0 }, { 10, 2, 0 }, { 10, 2, 2 }, { 16, 3, 0 }, { 10, 8, 0 } };

	public RandomNumber(Session session, int taskid, int userid) {
		this.taskNumberDAO = DAOFactory.TaskNumberDAOIf(session);
		this.taskid = taskid;
		this.userid = userid;
		this.submissionid = 0;
		//this.submissionid = -1;
		this.taskNumbers = new ArrayList<TaskNumber>();
	}

	public RandomNumber(int taskid, int userid, int submissionid) {
		this.taskid = taskid;
		this.userid = userid;
		this.submissionid = submissionid;
		this.taskNumbers = new ArrayList<TaskNumber>();
	}

	public List<TaskNumber> getTaskNumbers() {
		return taskNumbers;
	}

	public void setTaskNumbers(ArrayList<TaskNumber> taskNumbers) {
		this.taskNumbers = taskNumbers;
	}

	public List<TaskNumber> setSubmissionid(int submissionid) {
		this.submissionid = submissionid;
		ListIterator<TaskNumber> it = this.taskNumbers.listIterator(this.taskNumbers.size());
		while (it.hasPrevious()) {
			it.previous().setSubmissionid(submissionid);
		}
		return this.taskNumbers;
	}

	public String getTaskDescription() {
		return this.description;
	}

	public String setTaskDescription(String taskDescription, List<TaskNumber> taskNumberList) {
		this.description = taskDescription;
		for (TaskNumber taskNumber : taskNumberList) {
			this.replaceDescription(this.description, taskNumber.getType(), taskNumber.getNumber());
		}
		return this.description;
	}

	public String setTaskDescription(String taskDescription) {
		this.description = taskDescription;
		for (int i = 0; i < this.types.length; i++) {
			this.description = this.replaceDescription(description, this.types[i], this.randomParam[i]);
		}
		return this.description;
	}

	private String replaceDescription(String description, char type, int[] randomParam) {
		while (description.contains("-" + type + "-") == true) {
			String number = getRandomNumber(randomParam);
			if (this.submissionid != 0) {
				this.taskNumbers.add(new TaskNumber(this.taskid, this.userid, this.submissionid, number, type));
			} else {
				this.taskNumbers.add(new TaskNumber(this.taskid, this.userid, number, type));
			}
			description = description.replaceFirst("-" + type + "-", this.getNumber(number, randomParam));
		}
		return description;
	}

	private String replaceDescription(String description, char type, String number) {
		this.description = description.replaceFirst("-" + type + "-", this.getNumber(number, this.getRandonParam(type)));
		return this.description;
	}

	/**
	 * @param type, type of numeral system representation(2 for binary etc)
	 * @param beforeComa, number of positions before the decimal point
	 * @param afterComa, number of positions after the decimal point
	 * @return str, String with the representation of the number
	 */
	private String getRandomNumber(int[] randomParam) {
		String before = "1";
		String str = null;
		for (int i = 0; i < randomParam[1]; i++) {
			before = before + "0";
		}
		if (randomParam[1] > 6) {
			str = Double.toString(Math.random()).replace(".", "");
			str = str.substring(0, randomParam[1]);
			while (str.startsWith("0") == true) {
				str = str.replaceFirst("0", "");
			}
		} else {
			double zahl = (double) (Math.random() * Integer.parseInt(before));
			str = String.valueOf(Math.round(zahl));
			if (randomParam[2] > 0) {
				String after = "##0.";
				for (int i = 0; i < randomParam[2]; i++) {
					after = after + "0";
				}
				DecimalFormat df = new DecimalFormat(after);
				str = df.format(zahl);
				str = str.replace(',', '.');
			}
		}
		return str;
	}

	public int[] getRandonParam(char type) {
		int pos = 0;
		for (int i = 0; i < this.types.length; i++) {
			if (type == this.types[i]) {
				pos = i;
				i = this.types.length;
			}
		}
		return this.randomParam[pos];
	}

	public String getNumber(String number, int[] randomParam) {
		if (randomParam[1] > 6) {
			BigInteger big = new BigInteger(number);
			big.byteValue();
			number = number + " (vereinfacht: " + get2Skalierung(big) + ")";
		} else {
			double zahl = Double.parseDouble(number);
			switch (randomParam[0]) {
				case 2:
					if (randomParam[2] == 0) {
						number = String.valueOf(Integer.toBinaryString((int) zahl));
					} else {
						number = String.valueOf(Integer.toBinaryString(Float.floatToRawIntBits((float) zahl)));
					}
					break;
				case 8:
					number = String.valueOf(Integer.toOctalString(Float.floatToRawIntBits((float) zahl)));
					break;
				case 10:
					if (randomParam[2] == 0) {
						number = String.valueOf((int) zahl);
					} else {
						number = String.valueOf(zahl);
					}
					break;

				case 16:
					number = String.valueOf(Double.toHexString(zahl));
					break;
			}
		}
		return number;
	}

	private String get2Skalierung(BigInteger number) {
		BigInteger result = number;
		BigInteger div = new BigInteger("2");
		int scale = -1;
		BigInteger rest = new BigInteger("0");
		BigInteger cero = new BigInteger("0");
		while (!result.equals(cero)) {
			result = result.divide(div);
			scale += 1;
		}
		rest = number.subtract(div.pow(scale));
		return "(2^" + Integer.toString(scale) + ") + " + rest;
	}
}
