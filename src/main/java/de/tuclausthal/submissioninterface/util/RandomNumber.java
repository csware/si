/*
 * Copyright 2011 Giselle Rodriguez
 * Copyright 2011, 2017, 2020-2021, 2024 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.util;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class RandomNumber {
	private static final char[] types = { 'B', 'O', 'D', 'F', 'H', 'L' };
	private static final int[][] randomParam = { { 2, 3, 0 }, { 8, 100, 0 }, { 10, 100, 0 }, { 10, 100, 2 }, { 16, 1000, 0 }, { 10, 100000000, 0 } };

	public static String getRandomNumber(int[] randomParam) {
		StringBuilder before = new StringBuilder("1");
		String str = null;
		for (int i = 0; i < randomParam[1]; i++) {
			before.append("0");
		}
		if (randomParam[1] > 6) {
			str = Double.toString(Math.random()).replace(".", "");
			str = str.substring(0, randomParam[1]);
			while (str.startsWith("0") == true) {
				str = str.replaceFirst("0", "");
			}
		} else {
			double zahl = Math.random() * Integer.parseInt(before.toString());
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

	public static String binStringToHex(String bin) {
		if (bin.length() % 4 != 0) {
			return null;
		}
		StringBuilder hex = new StringBuilder();
		for (int i = 0; i * 4 < bin.length(); i++) {
			if ("0000".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("0");
			} else if ("0001".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("1");
			} else if ("0010".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("2");
			} else if ("0011".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("3");
			} else if ("0100".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("4");
			} else if ("0101".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("5");
			} else if ("0110".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("6");
			} else if ("0111".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("7");
			} else if ("1000".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("8");
			} else if ("1001".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("9");
			} else if ("1010".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("A");
			} else if ("1011".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("B");
			} else if ("1100".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("C");
			} else if ("1101".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("D");
			} else if ("1110".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("E");
			} else if ("1111".equals(bin.substring(i * 4, (i + 1) * 4))) {
				hex.append("F");
			} else {
				return null;
			}
		}
		return hex.toString();
	}

	public static String trimLeadingZeros(String str) {
		if (str == null) {
			return null;
		}
		while (str.length() > 0 && str.charAt(0) == '0') {
			str = str.substring(1);
		}
		return str;
	}

	public static String getFloatBits(float randomNumber) {
		String bits = Integer.toBinaryString(Float.floatToIntBits(randomNumber));
		while (bits.length() < 32) {
			bits = "0" + bits;
		}
		return bits;
	}

	public static String[] getFloatBitsTruncated(float randomNumber) {
		String bits = getFloatBits(randomNumber);

		int shift = 0;
		if (randomNumber > 1) {
			shift = (int) Math.ceil(Math.log(((Float) randomNumber).intValue()));
		}
		bits = bits.substring(0, 8 + 5 + shift);

		while (bits.length() < 32) {
			bits += "0";
		}

		String origNumber = "";
		if (bits.charAt(0) == '1') {
			origNumber = "-" + String.valueOf(Float.intBitsToFloat(Integer.parseInt(bits.substring(1), 2)));
		} else {
			origNumber = String.valueOf(Float.intBitsToFloat(Integer.parseInt(bits, 2)));
		}

		return new String[] { origNumber, bits };
	}

	public static float getFloat(boolean allowNegative) {
		double randomNumber = Math.random() * 100;
		if (allowNegative && Math.random() >= 0.7) {
			randomNumber *= -1;
		}
		return (float) randomNumber;
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
		BigInteger div = BigInteger.TWO;
		int scale = -1;
		BigInteger rest = BigInteger.ZERO;
		BigInteger cero = BigInteger.ZERO;
		while (!result.equals(cero)) {
			result = result.divide(div);
			scale += 1;
		}
		rest = number.subtract(div.pow(scale));
		return "(2^" + Integer.toString(scale) + ") + " + rest;
	}
}
