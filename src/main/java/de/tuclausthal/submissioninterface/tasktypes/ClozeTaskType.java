/*
 * Copyright 2021-2022 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.tasktypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;

import de.tuclausthal.submissioninterface.util.Util;

public class ClozeTaskType {
	private final static Pattern itemsPattern = Pattern.compile("\\{((?:MULTICHOICE|SHORTANSWER|SHORTANSWER_NC|NUMERICAL).+?)\\}", Pattern.MULTILINE | Pattern.DOTALL);
	private final static Pattern itemPattern = Pattern.compile("^([^:]+):(.*$)", Pattern.MULTILINE | Pattern.DOTALL);

	private final static String FORM_NAME = "cloze";

	private final static int MAX_INPUTLENGTH = 200;
	public final static String FIXED_LIMIT = " maxlength=\"" + MAX_INPUTLENGTH + "\"";

	final private StringBuilder sb;
	final private List<ClozeItem> items = new ArrayList<>();

	private String error;

	public ClozeTaskType(String text, List<String> oldInput, boolean notEditable, boolean feedback) {
		assert (notEditable || !feedback);
		Matcher m = itemsPattern.matcher(text);

		int i = 0;
		int start = 0;
		sb = new StringBuilder();
		try {
			while (m.find()) {
				sb.append(text.subSequence(start, m.start()));
				Matcher outerMatcher = itemPattern.matcher(m.group(1));
				if (!outerMatcher.matches()) {
					throw new RuntimeException("no valid Cloze: \"" + outerMatcher.group(1) + "\"");
				}
				ClozeItem item;
				String data = StringEscapeUtils.unescapeHtml4(outerMatcher.group(2));
				switch (outerMatcher.group(1)) {
					case "MULTICHOICE":
						item = new MultipleChocieClozeItem(data);
						break;
					case "SHORTANSWER":
						item = new ShortAnswerClozeItem(data);
						break;
					case "SHORTANSWER_NC":
						item = new ShortAnswerIgnoreCaseClozeItem(data);
						break;
					case "NUMERICAL":
						item = new NumericClozeItem(data);
						break;
					default:
						throw new RuntimeException("no valid Cloze type: \"" + outerMatcher.group(1) + "\" in \"" + m.group(1) + "\"");
				}
				item.appendToHTML(sb, i, oldInput == null ? null : (oldInput.size() <= i ? "" : oldInput.get(i)), notEditable, feedback);
				items.add(item);
				start = m.end();
				++i;
			}
			sb.append(text.subSequence(start, text.length()));
		} catch (RuntimeException e) {
			sb.setLength(0);
			sb.append("Konnte Cloze nicht parsen.");
			error = e.getMessage();
		}
	}

	public boolean hasError() {
		return error != null;
	}

	public String getError() {
		return error;
	}

	public int getClozeEntries() {
		return items.size();
	}

	public String toHTML() {
		return Util.makeCleanHTML(sb.toString());
	}

	public boolean isAutoGradeAble() {
		if (items.isEmpty()) {
			return false;
		}
		return items.stream().allMatch(item -> item.isAutoGradeAble());
	}

	public boolean isAutoGradeAble(int i) {
		return items.get(i).isAutoGradeAble();
	}

	public String getCorrect(int index) {
		ClozeItem item = items.get(index);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < item.knownOptions.size(); ++i) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(item.knownOptions.get(i));
			sb.append(": ");
			sb.append(item.knownPoints.get(i));
		}
		return sb.toString();
	}

	public int maxPoints() {
		int points = 0;
		for (ClozeItem item : items) {
			points += item.maxPoints();
		}
		return points;
	}

	public int calculatePoints(int index, String result) {
		return items.get(index).calculatePoints(result);
	}

	public int maxPoints(int index) {
		return items.get(index).maxPoints();
	}

	public int calculatePoints(List<String> results) {
		int points = 0;
		for (int i = 0; i < results.size(); ++i) {
			points += items.get(i).calculatePoints(results.get(i));
		}
		return points;
	}

	public List<String> parseResults(HttpServletRequest request) {
		List<String> results = new ArrayList<>();
		for (int i = 0; i < getClozeEntries(); ++i) {
			String result = request.getParameter(FORM_NAME + i);
			if (result == null) {
				result = "";
			}
			results.add(result.trim());
		}
		return results;
	}

	abstract public static class ClozeItem {
		final static protected Pattern optionPattern = Pattern.compile("^(\\d+|\\d+\\.\\d+|\\.\\d+)=(.*)");

		final protected List<String> knownOptions = new ArrayList<>();
		final protected List<String> knownPoints = new ArrayList<>();

		public boolean isAutoGradeAble() {
			return !knownOptions.isEmpty();
		}

		abstract public void appendToHTML(StringBuilder sb, int number, String data, boolean notEditable, boolean feedback);

		public int calculatePoints(String input) {
			for (int i = 0; i < knownOptions.size(); ++i) {
				if (knownOptions.get(i).equals(input)) {
					return Util.convertToPoints(knownPoints.get(i));
				}
			}
			return 0;
		}

		public int maxPoints() {
			if (knownPoints.isEmpty()) {
				return 0;
			}
			return knownPoints.stream().mapToInt(points -> Util.convertToPoints(points)).max().getAsInt();
		}
	}

	public static class StringTuple {
		String element1;
		String element2;

		public StringTuple(String element1, String element2) {
			this.element1 = element1;
			this.element2 = element2;
		}
	}

	public static class MultipleChocieClozeItem extends ClozeItem {
		public MultipleChocieClozeItem(String data) {
			ArrayList<StringTuple> options = new ArrayList<>();
			for (String option : data.split("~")) {
				Matcher parsedOption = optionPattern.matcher(option);
				if (!parsedOption.matches() || parsedOption.group(2).isBlank()) {
					throw new RuntimeException("Unparseable option \"" + option + "\"found in \"" + data + "\"");
				}
				options.add(new StringTuple(parsedOption.group(2), Objects.toString(parsedOption.group(1), "0")));
			}
			if (options.isEmpty()) {
				throw new RuntimeException("No options found in \"" + data + "\"");
			}
			options.sort((o1, o2) -> o1.element1.compareTo(o2.element1));
			options.forEach(option -> {
				knownOptions.add(option.element1);
				knownPoints.add(option.element2);
			});
		}

		@Override
		public void appendToHTML(StringBuilder sb, int number, String oldData, boolean notEditable, boolean feedback) {
			if (!notEditable) {
				sb.append("<select size=1 name=\"");
				sb.append(FORM_NAME);
				sb.append(number);
				sb.append("\"");
				if (notEditable) {
					sb.append(" disabled");
				}
				sb.append(">");
				sb.append("<option value=\"\"></option>");

				for (String option : knownOptions) {
					sb.append("<option");
					sb.append(" value=\"" + Util.escapeHTML(option) + "\"");
					if (option.equals(oldData)) {
						sb.append(" selected");
					}
					sb.append(">");
					sb.append(Util.escapeHTML(option));
					sb.append("</option>");
				}
				sb.append("</select>");
			} else {
				sb.append("<input name=\"");
				sb.append(FORM_NAME);
				sb.append(number);
				sb.append("\" type=text disabled");
				if (oldData != null) {
					sb.append(" value=\"" + Util.escapeHTML(oldData) + "\"");
					sb.append(" size=" + (oldData.length() + 3));
				} else {
					sb.append(" size=20");
				}
				sb.append(">");
			}
			if (feedback) {
				sb.append(" <span class=\"cloze_points\">(➜ ");
				sb.append(Util.showPoints(calculatePoints(oldData)));
				sb.append(" Punkt(e))</span>");
			}
		}
	}

	public static class ShortAnswerClozeItem extends ClozeItem {
		int longestEntry = 0;

		public ShortAnswerClozeItem(String data) {
			if (data.isEmpty()) {
				return;
			}
			for (String option : data.split("~")) {
				Matcher parsedOption = optionPattern.matcher(option);
				if (!parsedOption.matches()) {
					throw new RuntimeException("Unparseable option \"" + option + "\"found in \"" + data + "\"");
				}
				knownPoints.add(parsedOption.group(1));
				knownOptions.add(parsedOption.group(2));
				longestEntry = Math.max(longestEntry, parsedOption.group(2).length());
				if (longestEntry > MAX_INPUTLENGTH) {
					throw new RuntimeException("Option is longer than allowed maxlength " + MAX_INPUTLENGTH + ": \"" + parsedOption.group(2) + "\"");
				}
			}
		}

		protected ShortAnswerClozeItem() {}

		@Override
		public void appendToHTML(StringBuilder sb, int number, String oldData, boolean notEditable, boolean feedback) {
			sb.append("<input name=\"");
			sb.append(FORM_NAME);
			sb.append(number);
			sb.append("\"");
			sb.append(" type=text");
			if (notEditable) {
				sb.append(" disabled");
			}
			int maxCurrentLength = longestEntry;
			if (oldData != null) {
				sb.append(" value=\"" + Util.escapeHTML(oldData) + "\"");
				maxCurrentLength = Math.max(maxCurrentLength, oldData.length());
				if (notEditable) {
					sb.append(" size=" + (oldData.length() + 3));
				}
			}
			if (!notEditable || oldData == null) {
				maxCurrentLength = Math.max(20, maxCurrentLength + 3);
				sb.append(" size=" + maxCurrentLength);
			}
			sb.append(FIXED_LIMIT);
			sb.append(" autocomplete=off>");
			if (feedback && isAutoGradeAble()) {
				sb.append(" <span class=\"cloze_points\">(➜ ");
				sb.append(Util.showPoints(calculatePoints(oldData)));
				sb.append(" Punkt(e))</span>");
			}
		}
	}

	public static class ShortAnswerIgnoreCaseClozeItem extends ShortAnswerClozeItem {
		public ShortAnswerIgnoreCaseClozeItem(String data) {
			super(data);
		}

		@Override
		public int calculatePoints(String input) {
			for (int i = 0; i < knownOptions.size(); ++i) {
				if (knownOptions.get(i).equalsIgnoreCase(input)) {
					return Util.convertToPoints(knownPoints.get(i));
				}
			}
			return 0;
		}
	}

	public static class NumericClozeItem extends ShortAnswerClozeItem {
		public NumericClozeItem(String data) {
			for (String option : data.split("~")) {
				Matcher parsedOption = optionPattern.matcher(option);
				if (!parsedOption.matches()) {
					throw new RuntimeException("Unparseable option \"" + option + "\"found in \"" + data + "\"");
				}
				knownPoints.add(parsedOption.group(1));
				knownOptions.add(parsedOption.group(2));
				longestEntry = Math.max(longestEntry, parsedOption.group(2).length());
				if (longestEntry > MAX_INPUTLENGTH) {
					throw new RuntimeException("Option is longer than allowed maxlength " + MAX_INPUTLENGTH + ": \"" + option + "\"");
				}
			}
		}

		@Override
		public int calculatePoints(String inputString) {
			double input = 0;
			try {
				input = Double.parseDouble(inputString);
				for (int i = 0; i < knownOptions.size(); ++i) {
					double value;
					double delta = Double.MIN_VALUE;
					int separator = knownOptions.get(i).indexOf(':');
					if (separator >= 1) {
						value = Double.parseDouble(knownOptions.get(i).substring(0, separator));
						delta = Double.parseDouble(knownOptions.get(i).substring(separator + 1));
					} else {
						value = Double.parseDouble(knownOptions.get(i));
					}
					if (value - delta <= input && input <= value + delta) {
						return Util.convertToPoints(knownPoints.get(i));
					}
				}
			} catch (NumberFormatException e) {
			}
			return 0;
		}
	}
}
