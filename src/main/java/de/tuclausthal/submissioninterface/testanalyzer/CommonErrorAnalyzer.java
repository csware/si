/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
 * Copyright 2021 Florian Holzinger <f.holzinger@campus.lmu.de>
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

package de.tuclausthal.submissioninterface.testanalyzer;

import java.io.StringReader;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.CommonErrorDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.testanalyzer.haskell.syntax.RegexBasedHaskellClustering;

public class CommonErrorAnalyzer {
	//from Literatur
	public final static List<String> commonCompileTimeErrors = List.of("illegal character", "';' expected", "'}' expected", "')' expected", "'(' expected", "'{' expected", "'[' expected", "']' expected", "illegal start of expression", "not a statement", "unclosed string literal", "reached end of file while parsing", "cannot find symbol", "is already defined in", "array required, but", "has private access in", "might not have been initialized", "cannot be applied to", "possible loss of precision", "incompatible types", "inconvertible type", "missing return statement", "missing return value", "cannot return a value from method whose result type is void", "invalid method declaration; return type required", "unreachable statement", "non-static variable", "non-static method", "error: unreported exception");
	public final static List<String> commonRunTimeErrors = List.of("StringIndexOutOfBoundsException", "ArrayIndexOutOfBoundsException", "NullPointerException", "InputMismatchException", "IllegalFormatException", "NumberFormatException", "ArithmeticException", "OutOfMemoryError", "StackOverflowError", "NoClassDefFoundError", "NoSuchMethodFoundError");

	//from DB
	public final static List<String> commonCompileTimeErrorsDB = List.of("does not exist", "<identifier> expected", "class expected", "class, interface, or enum expected", "'.class' expected", ": expected", "illegal start of type", "is public, should be declared in a file named", "illegal escape character", "bad operand type", "cannot be instantiated", "'else' without 'if'", "array dimension missing", "'try' without 'catch' or 'finally'", "inner classes cannot have static declarations", "bad operand types for binary operator", "missing method body, or declare abstract", "is not abstract and does not override abstract method", "an enclosing instance that contains", "no suitable constructor found for", "'void' type not allowed here", "no suitable method found for", "duplicate class:", "abstract methods cannot have a body", "method does not override or implement a method from a supertype", "is never thrown in body of corresponding try statement", "unmappable character", "cyclic inheritance involving", "is not visible", "unclosed comment", "for-each not applicable to expression type", "modifier static not allowed here", "is accessed from within inner class; needs to be declared final", "class, interface, enum, or record expected", "Illegal static declaration in inner class", "cannot be dereferenced", "attempting to assign weaker access privileges", "is ambiguous", "'.' expected", "enum types may not be instantiated", "is never thrown in body of corresponding try statement", "method does not override or implement a method from a supertype", "-> expected");
	public final static List<String> commonRunTimeErrorsDB = List.of("ClassNotFoundException", "InvocationTargetException", "InterruptedException", "NoSuchMethodException", "IllegalStateException", "ClassCastException", "IllegalArgumentException", "IllegalFormatConversionException", "AssertionFailedError", "ParseException", "FileNotFoundException", "IllegalAccessError", "NoSuchMethodError", "AccessControlException", "NoSuchFieldError", "UnsupportedClassVersionError", "Student-program aborted due to too long execution time", "NoSuchElementException", "AssertionError", "IncompleteAnnotationException", "InvalidModuleDescriptorException", "ResolutionException", "WrongMethodTypeException", "StringConcatException", "GenericSignatureFormatError", "InaccessibleObjectException", "MalformedParameterizedTypeException", "MalformedParametersException", "TypeNotPresentException", "java.lang.UnknownError", "UnsatisfiedLinkError", "UndeclaredThrowableException", "AbstractMethodError", "BootstrapMethodError", "ClassFormatError", "CloneNotSupportedException", "EnumConstantNotPresentException", "ExceptionInInitializerError", "LinkageError", "IllegalAccessException", "IllegalCallerException", "IllegalMonitorStateException", "IllegalThreadStateException", "IncompatibleClassChangeError", "InstantiationError", "InstantiationException", "java.lang.InternalError", "LayerInstantiationException", "NegativeArraySizeException", "ClassCircularityError", "LambdaConversionException", "AnnotationFormatError", "NoSuchFieldException", "ReflectiveOperationException", "SecurityException", "AnnotationTypeMismatchException", "UnsupportedOperationException", "VerifyError", "VirtualMachineError", "ConnectException", "BindException", "MalformedURLException", "URISyntaxException", "BufferOverflowException", "BufferUnderflowException", "InvalidMarkException", "ReadOnlyBufferException", "NonReadableChannelException", "NonWritableChannelException", "CharacterCodingException", "IllegalCharsetNameException", "MalformedInputException", "UnmappableCharacterException", "UnsupportedCharsetException", "AccessDeniedException", "ClosedDirectoryStreamException", "ClosedFileSystemException", "DirectoryIteratorException", "DirectoryNotEmptyException", "FileAlreadyExistsException", "FileSystemNotFoundException", "InvalidPathException", "NoSuchFileException", "NotDirectoryException", "DateTimeException", "ZoneRulesException", "UnsupportedTemporalTypeException", "DateTimeParseException", "BrokenBarrierException", "RejectedExecutionException", "PatternSyntaxException", "DataFormatException", "ConcurrentModificationException", "DuplicateFormatFlagsException", "EmptyStackException", "FormatFlagsConversionMismatchException", "FormatterClosedException", "IllegalFormatArgumentIndexException", "IllegalFormatCodePointException", "IllegalFormatFlagsException", "IllegalFormatPrecisionException", "IllegalFormatWidthException", "IllformedLocaleException", "InvalidPropertiesFormatException", "MissingFormatArgumentException", "MissingFormatWidthException", "MissingResourceException", "TooManyListenersException", "UnknownFormatConversionException", "UnknownFormatFlagsException", "NoSuchPaddingException", "IllegalBlockSizeException", "java.lang.Error", "java.lang.Exception", "java.lang.RuntimeException");

	private Session session;

	public CommonErrorAnalyzer(final Session session) {
		this.session = session;
	}

	public void runAnalysis(final TestResult testResult) {
		final Test test = testResult.getTest();
		if (test instanceof CompileTest) {
			groupCompilerTestResults((CompileTest) test, testResult);
		} else if (test instanceof JavaAdvancedIOTest) {
			groupJavaIOTestResults((JavaAdvancedIOTest) test, testResult);
		} else if (test instanceof JUnitTest) {
			groupJUnitTestResults((JUnitTest) test, testResult);
		} else if (test instanceof HaskellSyntaxTest) {
			groupHaskellSyntaxTestResults((HaskellSyntaxTest) test, testResult);
		} else if (test instanceof HaskellRuntimeTest haskellRuntimeTest) {
			groupHaskellRuntimeTestResults(haskellRuntimeTest, testResult);
		} else if (test instanceof DockerTest) {
			groupDockerTestResults((DockerTest) test, testResult);
		}
	}

	private void groupCompilerTestResults(final CompileTest test, final TestResult testResult) {
		if (testResult.getPassedTest()) {
			return;
		}

		groupTestResultToCommonErrors(testResult, testResult.getTestOutput(), "");
	}

	private void bindCommonError(final TestResult testResult, final String commonErrorTitle, final String commonErrorName, final CommonError.Type errorType) {
		final CommonErrorDAOIf commonErrorDAO = DAOFactory.CommonErrorDAOIf(session);
		final CommonError commonError = commonErrorDAO.getCommonError(commonErrorTitle, testResult.getTest());
		if (commonError != null) {
			commonError.getTestResults().add(testResult);
		} else {
			commonErrorDAO.newCommonError(commonErrorTitle, commonErrorName, testResult, errorType);
		}
	}

	public int getCompileTestResultsNumberOfErrors(final TestResult testResult) {
		int numberOfErrors = 0;
		String testOutputString = testResult.getTestOutput();

		if (testOutputString.contains("error")) {
			String numberStr = "";
			int pos = testOutputString.lastIndexOf("error");
			pos -= 1;

			while (Character.isDigit(testOutputString.charAt(pos - 1))) {
				numberStr = testOutputString.charAt(pos - 1) + numberStr;
				pos -= 1;
			}
			numberOfErrors = Integer.parseInt(numberStr);
		}
		return numberOfErrors;
	}

	private void groupJavaIOTestResults(final JavaAdvancedIOTest test, final TestResult testResult) {
		if (testResult.getPassedTest()) {
			return;
		}

		final JsonObject testOutputJson = Json.createReader(new StringReader(testResult.getTestOutput())).readObject();
		if (testOutputJson.containsKey("syntaxerror")) {
			// prevent syntax errors from showing up again in IO tests, but allows syntax errors with test code through
			if ("student-code".equals(testOutputJson.getString("syntaxerror"))) {
				return;
			}
			groupTestResultToCommonErrors(testResult, testOutputJson.getString("stderr"), "");
			return;
		}

		final String[] keyStr = getJavaIOKeyStr(test, testResult, testOutputJson);
		if (testOutputJson.containsKey("stderr")) {
			keyStr[1] += ": ";
			groupTestResultToCommonErrors(testResult, testResult.getTestOutput(), keyStr[0] + keyStr[1]);
		} else {
			//NO ERROR found but not passed
			bindCommonError(testResult, keyStr[0] + keyStr[1], keyStr[1], null);

		}
	}

	private static String[] getJavaIOKeyStr(final JavaAdvancedIOTest test, final TestResult testResult, final JsonObject testOutputJson) {
		final JsonArray jsonArray = testOutputJson.getJsonArray("steps");

		String stepsStr = "";
		for (int i = 0; i < jsonArray.size(); i++) {
			if (!jsonArray.getJsonObject(i).getBoolean("ok")) {
				stepsStr += "\"" + test.getTestSteps().get(i).getTitle() + "\" failed + ";
			}
		}

		String keyStr = "";
		if (testOutputJson.containsKey("stderr")) {
			keyStr += ": ";
		}
		if (testOutputJson.getString("stdout").isEmpty())
			keyStr += "Output null ";
		if (testOutputJson.getBoolean("exitedCleanly"))
			keyStr += "exitedCleanly ";
		if (testOutputJson.containsKey("missing-tests"))
			keyStr += "missing Tests ";
		if (testOutputJson.containsKey("time-exceeded"))
			keyStr += "time-exceeded ";

		return new String[] { stepsStr, keyStr };
	}

	private void groupHaskellRuntimeTestResults(final HaskellRuntimeTest test, final TestResult testResult) {
		if (testResult.getPassedTest()) {
			return;
		}

		// TODO@CHW implement correctly

		final JsonObject testOutputJson = Json.createReader(new StringReader(testResult.getTestOutput())).readObject();

		if (testOutputJson.containsKey("exitCode") && testOutputJson.getInt("exitCode") != 0) {
			// TODO: maybe modify and use groupTestResultToCommonErrors()
			// TODO: exitCode != 0 on compile error and on runtime error
			bindCommonError(testResult, "Failed", "Failed", null);
		}
	}

	private void groupJUnitTestResults(JUnitTest test, final TestResult testResult) {
		for (final TestResult otherTestResult : testResult.getSubmission().getTestResults()) {
			if (!otherTestResult.getPassedTest() && otherTestResult.getTest() instanceof CompileTest) {
				//CompileTest failed -> no analyse of JUnit Test
				bindCommonError(testResult, "CompileTest failed", "CompileTest failed", null);
				return;
			}
		}

		if (testResult.getPassedTest()) {
			return;
		}

		groupTestResultToCommonErrors(testResult, testResult.getTestOutput(), getJUnitKeyStr(testResult));
	}

	private static String getJUnitKeyStr(final TestResult testResult) {
		final String testOutputString = testResult.getTestOutput();
		String substringToSearch = " ";

		int posNewLine = testOutputString.indexOf("\n");
		if (posNewLine >= 0) {
			substringToSearch = testOutputString.substring(0, posNewLine) + " ";
		}

		String keyStr = "";
		outerloop: for (int i = 0; i < substringToSearch.length() - 1; i++) {
			if (substringToSearch.charAt(i) == '.') {
				keyStr += ".";
				switch (substringToSearch.charAt(i + 1)) {
					case '.':
						break;
					case 'E':
						keyStr += "E";
						break;
					case 'F':
						keyStr += "F";
						break;
					default:
						keyStr += ": ";
						break outerloop;
				}
			} else if (i == substringToSearch.length() - 2) {
				keyStr += ": ";
			}
		}

		return keyStr;
	}

	public static int[] getJUnitDetails(final TestResult testResult) {
		//        0 = numberOfTests
		//        1 = numberOfErrors
		//        2 = numberOfFailures
		final int[] details = new int[3];
		for (int i = 0; i < details.length; i++) {
			details[i] = 0;
		}

		String substringToSearch = " ";
		final int posNewLine = testResult.getTestOutput().indexOf("\n");
		if (posNewLine >= 0) {
			substringToSearch = testResult.getTestOutput().substring(0, posNewLine) + " ";
		}

		outerloop: for (int i = 0; i < substringToSearch.length() - 1; i++) {
			if (substringToSearch.charAt(i) == '.') {
				details[0] += 1;
				switch (substringToSearch.charAt(i + 1)) {
					case '.':
						break;
					case 'E':
						details[1] += 1;
						break;
					case 'F':
						details[2] += 1;
						break;
					default:
						break outerloop;
				}
			}
		}

		return details;
	}

	private void groupTestResultToCommonErrors(TestResult testResult, String output, String keyStr) {
		final boolean assigned1 = assignOneTestResultToErrorTypes(testResult, output, commonRunTimeErrors, CommonError.Type.RunTimeError, keyStr);
		final boolean assigned2 = assignOneTestResultToErrorTypes(testResult, output, commonRunTimeErrorsDB, CommonError.Type.RunTimeError, keyStr);
		final boolean assigned3 = assignOneTestResultToErrorTypes(testResult, output, commonCompileTimeErrors, CommonError.Type.CompileTimeError, keyStr);
		final boolean assigned4 = assignOneTestResultToErrorTypes(testResult, output, commonCompileTimeErrorsDB, CommonError.Type.CompileTimeError, keyStr);

		if (!(assigned1 || assigned2 || assigned3 || assigned4)) {
			bindCommonError(testResult, keyStr + "Not Grouped", "Not Grouped", null);
		}
	}

	private boolean assignOneTestResultToErrorTypes(final TestResult testResult, final String testOutputString, final List<String> listCommonErrors, final CommonError.Type errorType, final String keyStr) {
		boolean foundErrorGroup = false;
		for (final String commonError : listCommonErrors) {
			if (testOutputString.contains(commonError)) {
				String suffix = "";
				if (commonError.equals("ArrayIndexOutOfBoundsException")) {
					final int pos = testOutputString.lastIndexOf("length");
					String strlength = "";
					strlength += Character.isDigit(testOutputString.charAt(pos + 7)) ? testOutputString.charAt(pos + 7) : "";
					strlength += Character.isDigit(testOutputString.charAt(pos + 8)) ? testOutputString.charAt(pos + 8) : "";
					strlength += Character.isDigit(testOutputString.charAt(pos + 9)) ? testOutputString.charAt(pos + 9) : "";
					strlength += Character.isDigit(testOutputString.charAt(pos + 10)) ? testOutputString.charAt(pos + 10) : "";

					if (Integer.parseInt((strlength)) > 0) {
						suffix += " > length";
					} else {
						suffix += " init error, length 0";
					}
				}

				bindCommonError(testResult, keyStr + errorType + ":  " + commonError + suffix, commonError, errorType);
				foundErrorGroup = true;
			}
		}
		return foundErrorGroup;
	}

	private void groupDockerTestResults(final DockerTest test, final TestResult testResult) {
		if (testResult.getPassedTest()) {
			return;
		}

		JsonObject testOutputJson = Json.createReader(new StringReader(testResult.getTestOutput())).readObject();
		String stderr = testOutputJson.containsKey("stderr") ? testOutputJson.getString("stderr") : "";

		String keyStr = "";

		if (testOutputJson.containsKey("exitCode")) {
			keyStr += "ExitCode: " + testOutputJson.getInt("exitCode") + " ";
		}
		if (testOutputJson.containsKey("time-exceeded") && testOutputJson.getBoolean("time-exceeded")) {
			keyStr += "Timeout ";
		}
		if (testOutputJson.containsKey("missing-tests")) {
			keyStr += "Missing tests ";
		}

		groupTestResultToCommonErrors(testResult, stderr, keyStr);
	}

	private void groupHaskellSyntaxTestResults(final HaskellSyntaxTest test, final TestResult testResult) {
		if (testResult.getPassedTest()) {
			return;
		}

		JsonObject testOutputJson = Json.createReader(new StringReader(testResult.getTestOutput())).readObject();
		String stderr = testOutputJson.containsKey("stderr") ? testOutputJson.getString("stderr") : "";

		String clusterResult = RegexBasedHaskellClustering.classify(stderr);

		String keyStr = "Syntax: " + clusterResult;

		CommonErrorDAOIf commonErrorDAO = DAOFactory.CommonErrorDAOIf(session);
		CommonError commonError = commonErrorDAO.getCommonError(keyStr, testResult.getTest());
		if (commonError != null) {
			commonError.getTestResults().add(testResult);
		} else {
			commonErrorDAO.newCommonError(keyStr, clusterResult, testResult, CommonError.Type.CompileTimeError);
		}

	}
}
