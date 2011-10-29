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

package de.tuclausthal.submissioninterface.util;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class RandomNumber {
	private static final char[] types = { 'B', 'O', 'D', 'F', 'H', 'L' };
	private static final int[][] randomParam = { { 2, 2, 0 }, { 8, 2, 0 }, { 10, 2, 0 }, { 10, 2, 2 }, { 16, 3, 0 }, { 10, 8, 0 } };

	/**
	 * @param type, type of numeral system representation(2 for binary etc)
	 * @param beforeComa, number of positions before the decimal point
	 * @param afterComa, number of positions after the decimal point
	 * @return str, String with the representation of the number
	 */
	public static String getRandomNumber(int[] randomParam) {
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

	public static int[] getRandomParam(char type) {
		int pos = 0;
		for (int i = 0; i < types.length; i++) {
			if (type == types[i]) {
				pos = i;
				break;
			}
		}
		return randomParam[pos];
	}

	public static String getNumber(String origNumber, int[] randomParam) {
		String number = origNumber;
		if (randomParam[1] > 6) {
			BigInteger big = new BigInteger(number);
			//big.byteValue();
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

	private static String get2Skalierung(BigInteger number) {
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
