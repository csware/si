/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Christian Wagner <christian.wagner@campus.lmu.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import static java.lang.Math.ceil;

import java.io.IOException;
import java.io.Serial;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTestIdentifier;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.testframework.tests.impl.DockerTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.ProcessOutputGrabber;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.TaskPath;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for clustering haskell submissions based on common errors (dynamic/runtime analysis).
 * This servlet allows advisors to automatically generate and modify test steps.
 *
 * @author Christian Wagner
 */
@GATEController
public class HaskellRuntimeTestManager extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Map<String, List<String>> CONSTRAINT_TO_TYPES = new HashMap<>();

	static {
		CONSTRAINT_TO_TYPES.put("Eq", Arrays.asList("Bool", "Char", "Ordering", "Int", "Float", "Double", "String"));
		CONSTRAINT_TO_TYPES.put("Ord", Arrays.asList("Bool", "Char", "Ordering", "Int", "Float", "Double", "String"));
		CONSTRAINT_TO_TYPES.put("Show", Arrays.asList("Bool", "Char", "Ordering", "Int", "Float", "Double", "String"));
		CONSTRAINT_TO_TYPES.put("Read", Arrays.asList("Bool", "Char", "Ordering", "Int", "Float", "Double", "String"));

		CONSTRAINT_TO_TYPES.put("Enum", Arrays.asList("Bool", "Char", "Ordering", "Int", "Float", "Double"));
		CONSTRAINT_TO_TYPES.put("Bounded", Arrays.asList("Int", "Char", "Bool", "Ordering"));

		CONSTRAINT_TO_TYPES.put("Num", Arrays.asList("Int", "Float", "Double"));
		CONSTRAINT_TO_TYPES.put("Integral", List.of("Int"));
		CONSTRAINT_TO_TYPES.put("Real", Arrays.asList("Int", "Float", "Double"));
		CONSTRAINT_TO_TYPES.put("Fractional", Arrays.asList("Float", "Double"));
		CONSTRAINT_TO_TYPES.put("RealFrac", Arrays.asList("Float", "Double"));
		CONSTRAINT_TO_TYPES.put("Floating", Arrays.asList("Float", "Double"));
		CONSTRAINT_TO_TYPES.put("RealFloat", Arrays.asList("Float", "Double"));

		CONSTRAINT_TO_TYPES.put("Semigroup", Arrays.asList("[Int]", "String", "Ordering"));
		CONSTRAINT_TO_TYPES.put("Monoid", Arrays.asList("[Int]", "String", "Ordering"));

		CONSTRAINT_TO_TYPES.put("Functor", List.of("Maybe"));
		CONSTRAINT_TO_TYPES.put("Applicative", List.of("Maybe"));
		CONSTRAINT_TO_TYPES.put("Monad", List.of("Maybe"));
		CONSTRAINT_TO_TYPES.put("Foldable", List.of("Maybe"));
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		getServletContext().getNamedDispatcher(DockerTestManager.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test test = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (!(test instanceof HaskellRuntimeTest haskellRuntimeTest)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), haskellRuntimeTest.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("browseModelSolution".equals(request.getParameter("action"))) {
			deleteStoredClassifiedIdentifiers(haskellRuntimeTest, session);
			try {
				browseModelSolutionAndStoreClassifiedIdentifiers(haskellRuntimeTest, session);
			} catch (IOException e) {
				request.getSession().setAttribute("haskellRuntimeTestBrowseError", e.getMessage());
			}
			response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
		} else if ("addFunction".equals(request.getParameter("action"))) {
			String functionName = request.getParameter("functionName");
			String functionType = request.getParameter("functionType");

			if (functionName != null && !functionName.isEmpty() && functionType != null && !functionType.isEmpty()) {
				Transaction tx = session.beginTransaction();
				functionName = functionName.strip();
				functionType = normalizeTypeSignature(functionType).strip();

				HaskellRuntimeTestIdentifier newFunctionIdentifier = new HaskellRuntimeTestIdentifier(haskellRuntimeTest, "function");
				newFunctionIdentifier.setFunctionName(functionName);
				newFunctionIdentifier.setFunctionType(functionType);

				String typeSignatureWithConcreteTypesForKnownConstraints = deriveConcreteTypesForConstrainedTypeVariables(functionType);
				String concreteTypeSignature = replaceUnconstrainedTypeVariables(typeSignatureWithConcreteTypesForKnownConstraints, HaskellPrimitiveType.Int);
				newFunctionIdentifier.setFunctionConcreteType(concreteTypeSignature);

				session.persist(newFunctionIdentifier);

				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
		} else if ("deleteHaskellIdentifiers".equals(request.getParameter("action"))) {
			deleteStoredClassifiedIdentifiers(haskellRuntimeTest, session);
			response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
		} else if ("generateFunctionTestcases".equals(request.getParameter("action"))) {
			int identifierId = Util.parseInteger(request.getParameter("identifierid"), -1);
			int numberOfTestSteps = Util.parseInteger(request.getParameter("numberOfTestSteps"), 0);

			if (identifierId != -1 && numberOfTestSteps > 0) {
				try {
					List<DockerTestStepData> dockerTestStepDatas = readClassifiedIdentifiersAndGenerateFunctionTestcases(haskellRuntimeTest, identifierId, numberOfTestSteps);

					Transaction tx = session.beginTransaction();
					for (DockerTestStepData dockerTestStepData : dockerTestStepDatas) {
						// NOTE: DockerTestStep title is used for storing the function signature (needed for grouping the testcases in the view)
						String title = dockerTestStepData.getFunctionNameWithType();
						String testCode = dockerTestStepData.getTestCode();
						String expectedValue = dockerTestStepData.getExpectedValue();

						DockerTestStep newStep = new DockerTestStep(haskellRuntimeTest, title, testCode, expectedValue);
						session.persist(newStep);
					}

					tx.commit();
				} catch (IOException | IllegalArgumentException e) {
					request.getSession().setAttribute("haskellRuntimeTestGenerateError", e.getMessage());
				}
			}
			response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
		} else if ("editSingleTestStep".equals(request.getParameter("action"))) {
			String[] selectedIds = request.getParameterValues("selectedTestStepIds");

			if (selectedIds != null) {
				Set<Integer> testStepIds = Arrays.stream(selectedIds).map(s -> Util.parseInteger(s, -1)).filter(i -> i != -1).collect(Collectors.toSet());

				if (testStepIds.size() == 1) {
					int testStepId = testStepIds.iterator().next();
					String userEnteredFunctionCall = request.getParameter("userEnteredFunctionCall" + testStepId);

					if (userEnteredFunctionCall != null) {
						try {
							userEnteredFunctionCall = userEnteredFunctionCall.replaceAll("\r\n", "\n");

							List<String> expectedValues = computeExpectedValues(List.of(userEnteredFunctionCall), haskellRuntimeTest.getTask());
							if (expectedValues.size() != 1) {
								throw new AssertionError(String.format("Expected values: %d (only one function call)", expectedValues.size()));
							}
							String expectedValue = expectedValues.get(0);

							DockerTestStep stepToEdit = null;
							for (DockerTestStep step : haskellRuntimeTest.getTestSteps()) {
								if (step.getTeststepid() == testStepId) {
									stepToEdit = step;
									break;
								}
							}
							if (stepToEdit != null) {
								Transaction tx = session.beginTransaction();
								stepToEdit.setTestcode(DockerTestStepData.getTestcodeFromFunctionCallAndFilename(userEnteredFunctionCall, getModelSolutionFilename(haskellRuntimeTest)));
								stepToEdit.setExpect(DockerTestStepData.getExpectedValueFromGeneratorExpectedValue(expectedValue));
								tx.commit();
							}
						} catch (IOException | AssertionError e) {
							request.getSession().setAttribute("haskellRuntimeTestEditTestcaseError", e.getMessage());
						}
					}
				}
			}
			response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
		} else if ("duplicateMultipleTestSteps".equals(request.getParameter("action"))) {
			String[] selectedIds = request.getParameterValues("selectedTestStepIds");
			if (selectedIds != null) {
				Set<Integer> testStepIds = Arrays.stream(selectedIds).map(s -> Util.parseInteger(s, -1)).filter(i -> i != -1).collect(Collectors.toSet());
				duplicateTestStepsWithIds(haskellRuntimeTest, session, testStepIds);
			}
			response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
		} else if ("deleteMultipleTestSteps".equals(request.getParameter("action"))) {
			String[] selectedIds = request.getParameterValues("selectedTestStepIds");
			if (selectedIds != null) {
				Set<Integer> testStepIds = Arrays.stream(selectedIds).map(s -> Util.parseInteger(s, -1)).filter(i -> i != -1).collect(Collectors.toSet());
				deleteTestStepsWithIds(haskellRuntimeTest, session, testStepIds);
			}
			response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
		} else {
			getServletContext().getNamedDispatcher(DockerTestManager.class.getSimpleName()).forward(request, response);
		}
	}

	private static class DockerTestStepData {
		private final String functionNameWithType;
		private final String testCode;
		private final String expectedValue;

		private DockerTestStepData(String functionName, String functionType, String functionCall, String expectedValue, String filename) {
			this.functionNameWithType = functionName + " :: " + functionType;
			this.testCode = getTestcodeFromFunctionCallAndFilename(functionCall, filename);
			this.expectedValue = getExpectedValueFromGeneratorExpectedValue(expectedValue);
		}

		private static String getTestcodeFromFunctionCallAndFilename(String functionCall, String filename) {
			StringBuilder testCodeStringBuilder = new StringBuilder("ghci -XInstanceSigs");
			appendGhciEvaluateArgument(testCodeStringBuilder, ":set -package hashable");
			appendGhciEvaluateArgument(testCodeStringBuilder, ":m + Control.Exception Data.Hashable Data.List Data.Maybe System.Timeout");
			appendGhciEvaluateArgument(testCodeStringBuilder, ":load " + filename);
			appendGhciEvaluateArgument(testCodeStringBuilder, wrapGhciExpressionInCatchAndTimeout(functionCall.replaceAll("\r\n", "\n").strip()));
			return testCodeStringBuilder.toString();
		}

		private static String getExpectedValueFromGeneratorExpectedValue(String expectedValue) {
			return expectedValue.replaceAll("\r\n", "\n"); // TODO@CHW handle float values
		}

		private String getFunctionNameWithType() {
			return functionNameWithType;
		}

		private String getTestCode() {
			return testCode;
		}

		private String getExpectedValue() {
			return expectedValue;
		}
	}

	private static void deleteStoredClassifiedIdentifiers(HaskellRuntimeTest haskellRuntimeTest, Session session) {
		Transaction tx = session.beginTransaction();

		for (HaskellRuntimeTestIdentifier identifier : haskellRuntimeTest.getIdentifiers()) {
			session.remove(identifier);
		}

		tx.commit();
	}

	private static void browseModelSolutionAndStoreClassifiedIdentifiers(HaskellRuntimeTest haskellRuntimeTest, Session session) throws IOException {
		List<String> haskellIdentifiers = browseModelSolution(haskellRuntimeTest.getTask());
		HaskellClassifiedIdentifiers haskellClassifiedIdentifiers = classifyHaskellIdentifiers(haskellIdentifiers);

		Transaction tx = session.beginTransaction();

		for (HaskellNewtypeOrData haskellNewtypeOrData : haskellClassifiedIdentifiers.getNewtypesAndDatas()) {
			HaskellRuntimeTestIdentifier haskellRuntimeTestIdentifier = new HaskellRuntimeTestIdentifier(haskellRuntimeTest, "newtypeordata");

			haskellRuntimeTestIdentifier.setNewtypeOrDataTypename(haskellNewtypeOrData.getTypename());
			haskellRuntimeTestIdentifier.setNewtypeOrDataDefinition(haskellNewtypeOrData.getTypeDefinition());
			haskellRuntimeTestIdentifier.setNewtypeOrDataArbitraryInstance(haskellNewtypeOrData.getArbitraryInstance());

			session.persist(haskellRuntimeTestIdentifier);
		}

		for (HaskellFunction haskellFunction : haskellClassifiedIdentifiers.getFunctions()) {
			HaskellRuntimeTestIdentifier haskellRuntimeTestIdentifier = new HaskellRuntimeTestIdentifier(haskellRuntimeTest, "function");

			haskellRuntimeTestIdentifier.setFunctionName(haskellFunction.getName());
			haskellRuntimeTestIdentifier.setFunctionType(haskellFunction.getTypeSignature());

			String typeSignatureWithConcreteTypesForKnownConstraints = deriveConcreteTypesForConstrainedTypeVariables(haskellFunction.getTypeSignature());
			String concreteTypeSignature = replaceUnconstrainedTypeVariables(typeSignatureWithConcreteTypesForKnownConstraints, HaskellPrimitiveType.Int);
			haskellRuntimeTestIdentifier.setFunctionConcreteType(concreteTypeSignature);

			session.persist(haskellRuntimeTestIdentifier);
		}

		tx.commit();
	}

	private static List<DockerTestStepData> readClassifiedIdentifiersAndGenerateFunctionTestcases(HaskellRuntimeTest haskellRuntimeTest, int identifierId, int numberOfTestSteps) throws IOException, IllegalArgumentException {
		List<DockerTestStepData> generatedTestcases = new ArrayList<>();
		HaskellRuntimeTestIdentifier functionIdentifier = null;
		List<String> arbitraryInstances = new ArrayList<>();

		for (HaskellRuntimeTestIdentifier identifier : haskellRuntimeTest.getIdentifiers()) {
			switch (identifier.getIdentifierClass()) {
				case "newtypeordata":
					if (identifier.getNewtypeOrDataArbitraryInstance() != null) {
						arbitraryInstances.add(identifier.getNewtypeOrDataArbitraryInstance());
					}
					break;
				case "function":
					if (identifier.getIdentifierid() == identifierId) {
						functionIdentifier = identifier;
					}
					break;
			}
		}

		if (functionIdentifier != null) {
			// TODO@CHW maybe throw error if functionIdentifier does not contain all required fields / if some are null
			String functionName = functionIdentifier.getFunctionName();
			String functionType = functionIdentifier.getFunctionType();
			String functionConcreteType = functionIdentifier.getFunctionConcreteType();
			List<String> functionParameterTypes = getFunctionParameterTypes(functionConcreteType);

			if (functionParameterTypes.isEmpty()) {
				List<String> functionCalls = List.of(functionName);
				List<String> expectedValues = computeExpectedValues(functionCalls, haskellRuntimeTest.getTask());

				if (expectedValues.size() == 1) {
					String expectedValue = expectedValues.get(0);
					generatedTestcases.add(new DockerTestStepData(functionName, functionType, functionName, expectedValue, getModelSolutionFilename(haskellRuntimeTest)));
				}
				return generatedTestcases;
			}

			List<TestcaseWithTypes> testcases = generateQuickcheckFunctionTestcases(haskellRuntimeTest.getTask(), functionParameterTypes, arbitraryInstances, numberOfTestSteps);

			List<String> functionCalls = generateFunctionCalls(functionName, testcases);
			List<String> expectedValues = computeExpectedValues(functionCalls, haskellRuntimeTest.getTask());

			if (functionCalls.size() != expectedValues.size()) {
				throw new AssertionError(String.format("Expected values: %d, function calls: %d", expectedValues.size(), functionCalls.size()));
			}

			for (int i = 0; i < functionCalls.size(); i++) {
				generatedTestcases.add(new DockerTestStepData(functionName, functionType, functionCalls.get(i), expectedValues.get(i), getModelSolutionFilename(haskellRuntimeTest)));
			}
		} else {
			throw new IllegalArgumentException("Invalid identifier id.");
		}

		return generatedTestcases;
	}

	private static String getModelSolutionFilename(HaskellRuntimeTest haskellRuntimeTest) throws IOException {
		final Path taskPath = Util.constructPath(Configuration.getInstance().getDataPath(), haskellRuntimeTest.getTask());
		final Path modelSolutionPath = taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent());

		return getModelSolutionFile(modelSolutionPath).getFileName().toString();
	}

	private static void duplicateTestStepsWithIds(HaskellRuntimeTest haskellRuntimeTest, Session session, Set<Integer> testStepIds) {
		Transaction tx = session.beginTransaction();

		for (DockerTestStep step : haskellRuntimeTest.getTestSteps()) {
			if (testStepIds.contains(step.getTeststepid())) {
				session.persist(new DockerTestStep(haskellRuntimeTest, step.getTitle(), step.getTestcode(), step.getExpect()));
			}
		}

		tx.commit();
	}

	private static void deleteTestStepsWithIds(HaskellRuntimeTest haskellRuntimeTest, Session session, Set<Integer> testStepIds) {
		Transaction tx = session.beginTransaction();

		for (DockerTestStep step : haskellRuntimeTest.getTestSteps()) {
			if (testStepIds.contains(step.getTeststepid())) {
				session.remove(step);
			}
		}

		tx.commit();
	}

	private record SubprocessResult(String stdOut, String stdErr, int exitCode, boolean aborted) {
	}

	/** Evaluate haskell expressions using ghci expression evaluation mode (e.g. ghci -e "sum [1,2,3]").
	 *  Similar code in "de.tuclausthal.submissioninterface.testframework.tests.impl.DockerTest".
	 *  This code duplicate is justified for the following reasons:
	 *  1. The DockerTest is specifically designed for testing student submissions, and consists of a sequence
	 *     of DockerTestSteps that together evaluate a student submission. In contrast, this method evaluates
	 *     arbitrary ghci expressions that are used for generating testcases rather than for testing a submission.
	 *  2. The DockerTest includes logic to analyze the subprocess output, based on the DockerTestSteps it consists
	 *     of. Since this function evaluates arbitrary ghci expressions, this post-processing is not suitable here.
	 *
	 * @param packagesToEnable List of packages (e.g. ["QuickCheck"]), that need to be enabled (e.g. ":set -package QuickCheck")
	 * @param modulesToImport List of modules (e.g. ["Control.Monad", "Test.QuickCheck"]) that need to be imported (e.g. ":m + Control.Monad Test.QuickCheck")
	 * @param loadModelSolution Whether the model solution should be loaded into ghci (i.e. using "ghci :l")
	 * @param expressionsToEvaluate List of expressions (e.g. ["expr1", "expr2"]) that should be evaluated by ghci in this order (e.g. this will call "ghci -e expr1 -e expr2").
	 * @param task Task, for which the testcases should be generated based on the model solution
	 * @return result of the subprocess
	 */
	private static SubprocessResult evaluateWithGhci(String[] packagesToEnable, String[] modulesToImport, boolean loadModelSolution, String[] expressionsToEvaluate, Task task, boolean throwIOExceptionOnNonZeroExitCode) throws IOException {
		if (packagesToEnable == null)
			packagesToEnable = new String[0];
		if (modulesToImport == null)
			modulesToImport = new String[0];
		if (expressionsToEvaluate == null)
			expressionsToEvaluate = new String[0];

		final Path taskPath = Util.constructPath(Configuration.getInstance().getDataPath(), task);
		final Path modelSolutionPath = taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent());
		final int safeDockerTimeout = 45;

		Path generatorTempDir = null;
		try {
			generatorTempDir = Util.createTemporaryDirectory("haskellruntimegenerator");
			if (generatorTempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}

			final Path modelSolutionDir = generatorTempDir.resolve("modelsolution");
			Files.createDirectories(modelSolutionDir);

			final Path administrativeDir = generatorTempDir.resolve("administrative");
			Files.createDirectories(administrativeDir);

			if (Files.isDirectory(modelSolutionPath)) {
				Util.recursiveCopy(modelSolutionPath, modelSolutionDir);
			}

			Path hsFile = loadModelSolution ? getModelSolutionFile(modelSolutionDir) : null;

			// TODO@CHW: testCode is more complex in DockerTest
			StringBuilder testCode = new StringBuilder("ghci -XInstanceSigs");
			for (String packageToEnable : packagesToEnable) {
				appendGhciEvaluateArgument(testCode, ":set -package " + packageToEnable);
			}
			appendGhciEvaluateArgument(testCode, ":m + " + String.join(" ", modulesToImport));
			if (hsFile != null) {
				appendGhciEvaluateArgument(testCode, ":load " + hsFile.getFileName().toString());
			}
			for (String expression : expressionsToEvaluate) {
				appendGhciEvaluateArgument(testCode, expression);
			}

			final Path testDriver = administrativeDir.resolve("test.sh");
			try (Writer fw = Files.newBufferedWriter(testDriver)) {
				fw.write(testCode.toString());
			}

			List<String> params = new ArrayList<>();
			params.add("sudo");
			params.add(DockerTest.SAFE_DOCKER_SCRIPT);
			params.add("--timeout=" + safeDockerTimeout);
			params.add("--dir=" + Util.escapeCommandlineArguments(administrativeDir.toAbsolutePath().toString()));
			params.add("--");
			params.add("bash");
			params.add(Util.escapeCommandlineArguments(testDriver.toAbsolutePath().toString()));

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(modelSolutionDir.toFile());

			// only forward explicitly specified environment variables to test processes
			pb.environment().keySet().removeIf(key -> !("PATH".equalsIgnoreCase(key) || "USER".equalsIgnoreCase(key) || "LANG".equalsIgnoreCase(key)));

			LOG.debug("Executing external process: {} in {}", params, modelSolutionDir);

			Process process = pb.start();
			ProcessOutputGrabber outputGrabber = new ProcessOutputGrabber(process);

			int exitCode = -1;

			boolean aborted = false;
			try {
				exitCode = process.waitFor();
			} catch (InterruptedException e) {
				aborted = true;
			}

			if (exitCode == 23 || exitCode == 24) { // magic value of the safe-docker script (23=timeout, 24=oom)
				aborted = true;
			}

			try {
				outputGrabber.waitFor();
			} catch (InterruptedException e) {
				throw new IOException("Running haskell testcase generator failed");
			}

			String stdOut = outputGrabber.getStdOutBuffer().toString();
			String stdErr = outputGrabber.getStdErrBuffer().toString();

			if (throwIOExceptionOnNonZeroExitCode) {
				if (exitCode == 23) {
					throw new IOException("Running haskell testcase generator timed out (Timeout: " + safeDockerTimeout + "s)");
				} else if (exitCode == 24) {
					throw new IOException("Running haskell testcase generator failed (Out of memory)");
				} else if (exitCode != 0) {
					throw new IOException("Running haskell testcase generator failed with exit code " + exitCode + ". Output on stderr:\n" + stdErr);
				}
			}

			return new SubprocessResult(stdOut, stdErr, exitCode, aborted);
		} finally {
			if (generatorTempDir != null) {
				Util.recursiveDelete(generatorTempDir);
			}
		}
	}

	private static Path getModelSolutionFile(Path modelSolutionDirectory) throws IOException {
		// Expect exactly one .hs file among the modelsolution files -> this file will be used to generate the testcases
		try (Stream<Path> stream = Files.list(modelSolutionDirectory)) {
			List<Path> hsFiles = stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".hs")).toList();

			if (hsFiles.size() != 1) {
				throw new IOException("Expected exactly one model solution .hs file, found " + hsFiles.size() + " files.");
			} else {
				return hsFiles.get(0);
			}
		}
	}

	private static void appendGhciEvaluateArgument(StringBuilder testCode, String argument) {
		testCode.append(" -e '").append(argument.replace("'", "'\"'\"'").replace("\t", "    ")).append("'");
	}

	private static List<String> browseModelSolution(Task task) throws IOException {
		SubprocessResult result = evaluateWithGhci(null, null, true, new String[] { ":browse" }, task, true);
		return splitLinesButKeepMultilines(result.stdOut());
	}

	private static List<String> splitLinesButKeepMultilines(String resultStdout) {
		List<String> haskellIdentifiers = new ArrayList<>();

		for (String line : resultStdout.split("\\R")) {
			if (!(line.startsWith(" ") || line.startsWith("\t"))) {
				haskellIdentifiers.add(line);
			} else {
				// Handle multi-line formatting
				int lastIndex = haskellIdentifiers.size() - 1;
				if (lastIndex >= 0) {
					String updated = haskellIdentifiers.get(lastIndex) + "\n" + line;
					haskellIdentifiers.set(lastIndex, updated);
				}
			}
		}

		return haskellIdentifiers;
	}

	private static HaskellClassifiedIdentifiers classifyHaskellIdentifiers(List<String> haskellIdentifiers) {
		HaskellClassifiedIdentifiers classifiedIdentifiers = new HaskellClassifiedIdentifiers();

		for (String line : haskellIdentifiers) {
			if (line.startsWith("class")) {
				classifiedIdentifiers.addClass(line);
			} else if (line.startsWith("newtype") || line.startsWith("data")) {
				classifiedIdentifiers.addNewtypeOrData(line);
			} else if (line.contains("::") && !line.startsWith("type")) {
				classifiedIdentifiers.addFunction(line);
			}
		}

		return classifiedIdentifiers;
	}

	private static String deriveConcreteTypesForConstrainedTypeVariables(String functionTypeSignature) {
		if (functionTypeSignature.contains("::")) {
			functionTypeSignature = functionTypeSignature.split("::", 2)[1].trim();
		}

		functionTypeSignature = functionTypeSignature.trim();

		String[] parts = functionTypeSignature.split("=>");
		String constraintsPart;
		String typePart;

		if (parts.length == 1) {
			return functionTypeSignature;
		} else {
			constraintsPart = parts[0].trim();
			typePart = parts[1].trim();
		}

		Map<String, Set<String>> constraintsByTypeVariable = new HashMap<>();
		for (String c : constraintsPart.split(",")) {
			c = c.trim();
			c = c.replaceAll("[()]", "");
			if (c.isEmpty())
				continue;
			String[] constraintParts = c.split("\\s+");
			if (constraintParts.length == 2) {
				String constraint = constraintParts[0];
				String typeVariable = constraintParts[1];
				constraintsByTypeVariable.computeIfAbsent(typeVariable, k -> new HashSet<>()).add(constraint);
			}
		}

		Map<String, String> concreteTypeReplacementByTypeVariable = new HashMap<>();
		for (Map.Entry<String, Set<String>> e : constraintsByTypeVariable.entrySet()) {
			String typeVariable = e.getKey();
			Set<String> constraints = e.getValue();

			Set<String> possibleConcreteTypes = null;
			boolean noUnknownConstraints = true;

			for (String constraint : constraints) {
				List<String> concreteTypesForCurrentConstraint = CONSTRAINT_TO_TYPES.get(constraint);
				if (concreteTypesForCurrentConstraint == null) {
					noUnknownConstraints = false;
					break;
				}
				if (possibleConcreteTypes == null) {
					possibleConcreteTypes = new HashSet<>(concreteTypesForCurrentConstraint);
				} else {
					possibleConcreteTypes.retainAll(concreteTypesForCurrentConstraint);
				}
			}

			if (noUnknownConstraints && possibleConcreteTypes != null && !possibleConcreteTypes.isEmpty()) {
				List<String> prioritizedTypes = Arrays.asList("Int", "Char", "Double", "Bool", "[Int]", "Ordering");

				String chosen = null;
				for (String preferred : prioritizedTypes) {
					if (possibleConcreteTypes.contains(preferred)) {
						chosen = preferred;
						break;
					}
				}

				if (chosen == null) {
					chosen = possibleConcreteTypes.iterator().next();
				}
				concreteTypeReplacementByTypeVariable.put(typeVariable, chosen);
			}
		}

		StringBuilder newTypeSignature = new StringBuilder();

		List<String> remainingConstraints = new ArrayList<>();
		for (String constraint : constraintsPart.split(",")) {
			constraint = constraint.trim(); // e.g. "(Eq a"
			constraint = constraint.replaceAll("[()]", ""); // e.g. "Eq a"
			if (constraint.isEmpty())
				continue;
			String[] constraintParts = constraint.split("\\s+");
			if (constraintParts.length == 2) {
				String typeVariable = constraintParts[1]; // e.g. "a"
				if (!concreteTypeReplacementByTypeVariable.containsKey(typeVariable)) {
					remainingConstraints.add(constraint); // e.g. "Eq a"
				}
			}
		}

		if (!remainingConstraints.isEmpty()) {
			newTypeSignature.append("(").append(String.join(", ", remainingConstraints)).append(") => ");
		}

		String finalTypePart = typePart;
		for (Map.Entry<String, String> e : concreteTypeReplacementByTypeVariable.entrySet()) {
			String typeVariable = e.getKey();
			String concreteReplacementType = e.getValue();
			finalTypePart = finalTypePart.replaceAll("\\b" + Pattern.quote(typeVariable) + "\\b", concreteReplacementType);
		}

		newTypeSignature.append(finalTypePart);
		return normalizeTypeSignature(newTypeSignature.toString());
	}

	private static String normalizeTypeSignature(String typeSignature) {
		typeSignature = typeSignature.replace("\n", "");
		typeSignature = typeSignature.replaceAll("\\s*->\\s*", " -> ");
		return typeSignature.trim();
	}

	private static String replaceUnconstrainedTypeVariables(String typeSignature, HaskellPrimitiveType replacementType) {
		if (typeSignature.contains("::")) {
			typeSignature = typeSignature.split("::", 2)[1].trim();
		}

		return typeSignature.replaceAll("(?<![a-zA-Z])([a-z][a-zA-Z0-9]*)", replacementType.toString());
	}

	private static List<String> getFunctionParameterTypes(String concreteTypeSignature) {
		if (concreteTypeSignature.contains("::")) {
			concreteTypeSignature = concreteTypeSignature.split("::", 2)[1].trim();
		}
		if (concreteTypeSignature.contains("=>")) {
			throw new IllegalArgumentException("Function type signature should only contain concrete types.");
		}

		List<String> parts = splitExceptBetweenParentheses(concreteTypeSignature, '(', ')', "->");

		return parts.subList(0, parts.size() - 1); // remove return type (last element)
	}

	//TODO@CHW maybe replace all trim() by strip()?

	private static boolean parameterTypeIsFunction(String parameterType) {
		parameterType = parameterType.strip();
		return parameterType.contains("->") && parameterType.startsWith("(") && parameterType.endsWith(")");
	}

	private static String getFunctionReturnType(String concreteTypeSignature) {
		if (concreteTypeSignature.contains("::")) {
			concreteTypeSignature = concreteTypeSignature.split("::", 2)[1].strip();
		}
		if (concreteTypeSignature.contains("=>")) {
			throw new IllegalArgumentException("Function type signature should only contain concrete types.");
		}

		List<String> parts = splitExceptBetweenParentheses(concreteTypeSignature, '(', ')', "->");

		// TODO@CHW: this can throw index out of bounds exception?
		return parts.get(parts.size() - 1); // return type (last element)
	}

	private record TestcaseSingleParameterWithType(String testcaseParameter, String type) {
	}

	private record TestcaseWithTypes(List<TestcaseSingleParameterWithType> testcaseParametersWithTypes) {
	}

	private static List<TestcaseWithTypes> generateQuickcheckFunctionTestcases(Task task, List<String> functionParameterTypes, List<String> arbitraryInstances, int numberOfTestcases) throws IOException {
		if (functionParameterTypes.isEmpty()) {
			return List.of();
		}

		final String TESTCASE_SEPARATOR = "@NEXT-TESTCASE@"; // TODO@CHW: use random value in all separators
		final String TESTCASE_VALUE_SEPARATOR = "@NEXT-TESTCASE-VALUE@";

		/*
		 * Placeholder type avoids that ghci simplifies tuples. Example:
		 * - Gen ((Int, Int)) is automatically simplified to Gen (Int, Int)
		 * - => A single parameter of type (Int, Int) is considered as two parameters of type Int
		 * - Gen (PlaceholderT, (Int, Int)) is NOT simplified to Gen (PlaceholderT, Int, Int)
		 */
		final String PLACEHOLDER_TYPE_NAME = "Placeholder";
		final String CYCLIC_INT_MAP_TYPE_NAME = "CyclicIntMap";

		List<String> quickcheckParameterTypes = withCyclicIntMapTypes(withConstrainedPrimitiveTypes(functionParameterTypes), CYCLIC_INT_MAP_TYPE_NAME);

		List<String> parameterTypesTupleValues = new ArrayList<>();
		parameterTypesTupleValues.add(PLACEHOLDER_TYPE_NAME);
		parameterTypesTupleValues.addAll(quickcheckParameterTypes);

		// In ein Tuple-String umwandeln
		String parameterTypeTuple = "(" + String.join(", ", parameterTypesTupleValues) + ")";

		String placeholderType = String.format("""
				data %1$s = %1$s
				
				instance Show %1$s where
				  show %1$s = "@PLACEHOLDER@"
				
				instance Arbitrary %1$s where
				  arbitrary = return %1$s
				""", PLACEHOLDER_TYPE_NAME);

		String cyclicIntMap = String.format("""
				data %1$s target = %1$s {
				  name :: String,
				  cycleLength :: Int,
				  intMap :: Int -> target
				}
				
				instance Show target => Show (%1$s target) where
				  show (%1$s name cycleLength intMap) =
				    name
				      ++ " i = case i of "
				      ++ concatMap (liftM2 (++) show ((" -> " ++) . (++ "; ") . show . intMap)) [0 .. cycleLength - 1]
				      ++ "x -> "
				      ++ name
				      ++ " (abs (mod x "
				      ++ show cycleLength
				      ++ "))"
				
				instance Arbitrary target => Arbitrary (%1$s target) where
				  arbitrary = %1$s "cyclicIntMap" 50 <$> arbitrary
				""", CYCLIC_INT_MAP_TYPE_NAME);

		String safeAsciiValues = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String typenameCharOnlySafeAscii = HaskellConstrainedPrimitiveType.Char_OnlySafeAscii.toString();
		String typenameStringOnlySafeAscii = HaskellConstrainedPrimitiveType.String_OnlySafeAscii.toString();

		String charStringOnlySafeAscii = String.format("""
				safeAsciiChar :: Gen Char
				safeAsciiChar = elements "%1$s"
				
				newtype %2$s = %2$s Char
				
				instance Show %2$s where
				  show (%2$s c) = show c
				
				instance Arbitrary %2$s where
				  arbitrary = %2$s <$> safeAsciiChar
				
				newtype %3$s = %3$s String
				
				instance Show %3$s where
				  show (%3$s s) = show s
				
				instance Arbitrary %3$s where
				  arbitrary = %3$s <$> listOf safeAsciiChar
				""", safeAsciiValues, typenameCharOnlySafeAscii, typenameStringOnlySafeAscii);

		String toStringList = """
				class ToStringList a where toStringList :: a -> [String]
				
				instance (Show a, Show b) => ToStringList (a, b) where toStringList (a, b) = [show a, show b]
				
				instance (Show a, Show b, Show c) => ToStringList (a, b, c) where
				  toStringList (a, b, c) = [show a, show b, show c]
				
				instance (Show a, Show b, Show c, Show d) => ToStringList (a, b, c, d) where
				  toStringList (a, b, c, d) = [show a, show b, show c, show d]
				
				instance (Show a, Show b, Show c, Show d, Show e) => ToStringList (a, b, c, d, e) where
				  toStringList (a, b, c, d, e) = [show a, show b, show c, show d, show e]
				
				instance (Show a, Show b, Show c, Show d, Show e, Show f) => ToStringList (a, b, c, d, e, f) where
				  toStringList (a, b, c, d, e, f) = [show a, show b, show c, show d, show e, show f]
				
				instance (Show a, Show b, Show c, Show d, Show e, Show f, Show g) => ToStringList (a, b, c, d, e, f, g) where
				  toStringList (a, b, c, d, e, f, g) = [show a, show b, show c, show d, show e, show f, show g]
				
				instance (Show a, Show b, Show c, Show d, Show e, Show f, Show g, Show h) => ToStringList (a, b, c, d, e, f, g, h) where
				  toStringList (a, b, c, d, e, f, g, h) = [show a, show b, show c, show d, show e, show f, show g, show h]
				
				instance (Show a, Show b, Show c, Show d, Show e, Show f, Show g, Show h, Show i) => ToStringList (a, b, c, d, e, f, g, h, i) where
				  toStringList (a, b, c, d, e, f, g, h, i) = [show a, show b, show c, show d, show e, show f, show g, show h, show i]
				
				instance (Show a, Show b, Show c, Show d, Show e, Show f, Show g, Show h, Show i, Show j) => ToStringList (a, b, c, d, e, f, g, h, i, j) where
				  toStringList (a, b, c, d, e, f, g, h, i, j) = [show a, show b, show c, show d, show e, show f, show g, show h, show i, show j]
				""";

		String haskellCommand = String.format("""
				replicateM (%d) (generate (arbitrary :: Gen %s))
				  >>= putStrLn . intercalate testcaseSeparator . map
				        ( intercalate testcaseValueSeparator
				        . filter (/= show %s)
				        . toStringList
				        )
				""", numberOfTestcases, parameterTypeTuple, PLACEHOLDER_TYPE_NAME);

		List<String> expressionsToEvaluate = new ArrayList<>();
		expressionsToEvaluate.add("testcaseSeparator = \"" + TESTCASE_SEPARATOR + "\"");
		expressionsToEvaluate.add("testcaseValueSeparator = \"" + TESTCASE_VALUE_SEPARATOR + "\"");
		expressionsToEvaluate.add(placeholderType);
		expressionsToEvaluate.add(charStringOnlySafeAscii);
		expressionsToEvaluate.add(toStringList);
		expressionsToEvaluate.add(cyclicIntMap);
		expressionsToEvaluate.add(String.join("\n", arbitraryInstances));
		expressionsToEvaluate.add(haskellCommand);

		final String[] packagesToEnable = new String[] { "QuickCheck" };
		final String[] modulesToImport = new String[] { "Control.Monad", "Test.QuickCheck", "Data.List" };
		SubprocessResult result = evaluateWithGhci(packagesToEnable, modulesToImport, true, expressionsToEvaluate.toArray(new String[0]), task, true);

		String rawOutput = result.stdOut();
		if (rawOutput.endsWith("\n")) {
			rawOutput = rawOutput.substring(0, rawOutput.length() - 1);
		}

		List<List<String>> testcases = Arrays.stream(rawOutput.split(TESTCASE_SEPARATOR)).map(testcase -> Arrays.asList(testcase.split(TESTCASE_VALUE_SEPARATOR))).toList();

		List<TestcaseWithTypes> testcasesWithTypes = new ArrayList<>();

		for (List<String> testcase : testcases) {
			if (testcase.size() != functionParameterTypes.size()) {
				throw new AssertionError("Testcase length does not match function parameter types length");
			}

			List<TestcaseSingleParameterWithType> testcaseWithType = new ArrayList<>();

			for (int i = 0; i < testcase.size(); i++) {
				testcaseWithType.add(new TestcaseSingleParameterWithType(testcase.get(i), functionParameterTypes.get(i)));
			}

			for (int i = 0; i < testcaseWithType.size(); i++) {
				TestcaseSingleParameterWithType current = testcaseWithType.get(i);
				String testcaseValue = current.testcaseParameter();
				String testcaseValueType = current.type();

				if (parameterTypeIsFunction(testcaseValueType)) {
					// remove outer parentheses
					String innerType = testcaseValueType.strip();
					innerType = innerType.substring(1, innerType.length() - 1).strip();

					int numberOfParameters = getFunctionParameterTypes(innerType).size();

					String newTestcaseValue = embedCyclicIntMapInRandomFunction(numberOfParameters, testcaseValue);
					testcaseWithType.set(i, new TestcaseSingleParameterWithType(newTestcaseValue, testcaseValueType));
				}
			}
			testcasesWithTypes.add(new TestcaseWithTypes(testcaseWithType));
		}

		return testcasesWithTypes;
	}

	private static List<String> withConstrainedPrimitiveTypes(List<String> parameterTypes) {
		Map<String, String> replacementDict = Map.of(HaskellPrimitiveType.Char.toString(), HaskellConstrainedPrimitiveType.Char_OnlySafeAscii.toString(), HaskellPrimitiveType.String.toString(), HaskellConstrainedPrimitiveType.String_OnlySafeAscii.toString());

		String patternString = String.join("|", replacementDict.keySet().stream().map(key -> "(?<![A-Za-z])" + Pattern.quote(key) + "(?![A-Za-z0-9_])").toList());
		Pattern pattern = Pattern.compile(patternString);

		List<String> constrainedTypes = new ArrayList<>();

		for (String parameterType : parameterTypes) {
			Matcher matcher = pattern.matcher(parameterType);
			StringBuilder sb = new StringBuilder();

			while (matcher.find()) {
				String matchedKey = matcher.group();
				String replacement = replacementDict.get(matchedKey);
				matcher.appendReplacement(sb, replacement);
			}
			matcher.appendTail(sb);

			constrainedTypes.add(sb.toString());
		}

		return constrainedTypes;
	}

	private static List<String> withCyclicIntMapTypes(List<String> parameterTypes, String cyclicIntMapConstructor) {
		List<String> cyclicIntMapTypes = new ArrayList<>();

		for (String parameterType : parameterTypes) {
			if (parameterTypeIsFunction(parameterType)) {
				parameterType = parameterType.strip().substring(1, parameterType.length() - 1).strip();

				String returnType = getFunctionReturnType(parameterType);
				cyclicIntMapTypes.add(cyclicIntMapConstructor + " (" + returnType + ")");
			} else {
				cyclicIntMapTypes.add(parameterType);
			}
		}

		return cyclicIntMapTypes;
	}

	private static String embedCyclicIntMapInRandomFunction(int numberOfParameters, String cyclicIntMapDefinition) {
		String cyclicIntMapName = cyclicIntMapDefinition.split("\\s+")[0].trim();

		List<String> parameters = new ArrayList<>();
		for (int i = 0; i < numberOfParameters; i++) {
			parameters.add("p" + i);
		}

		return String.format("(let %s in let randomFunction %s = (%s . hash . show) (%s) in randomFunction)", cyclicIntMapDefinition, String.join(" ", parameters), cyclicIntMapName, String.join(", ", parameters));
	}

	private static List<String> generateFunctionCalls(String functionName, List<TestcaseWithTypes> testcasesWithTypes) {
		List<String> functionCalls = new ArrayList<>();

		for (TestcaseWithTypes testcaseWithTypes : testcasesWithTypes) {
			// testcaseWithTypes contains for example: [('9', 'Int'), ('Just 18', 'Maybe Int')]
			List<String> parts = new ArrayList<>();
			parts.add(functionName);

			for (TestcaseSingleParameterWithType testcaseSingleParameterWithType : testcaseWithTypes.testcaseParametersWithTypes()) {
				String value = testcaseSingleParameterWithType.testcaseParameter();
				String hsType = testcaseSingleParameterWithType.type();
				parts.add(String.format("(%s :: %s)", value, hsType));
			}

			String functionCall = String.join(" ", parts);
			functionCalls.add(functionCall);
		}

		return functionCalls;
	}

	private static List<String> computeExpectedValues(List<String> functionCalls, Task task) throws IOException {
		List<String> wrappedFunctionCalls = functionCalls.stream().map(HaskellRuntimeTestManager::wrapGhciExpressionInCatchAndTimeout).toList();

		String expectedValueSeparator = "@NEXT-EXPECTED-VALUE@";

		List<String> expressionsToEvaluate = new ArrayList<>();
		for (String wrappedFunctionCall : wrappedFunctionCalls) {
			expressionsToEvaluate.add(wrappedFunctionCall);
			expressionsToEvaluate.add(String.format("putStr \"%s\"", expectedValueSeparator));
		}

		String[] packagesToEnable = new String[] { "hashable" };
		String[] modulesToImport = new String[] { "Control.Exception Data.Hashable Data.List Data.Maybe System.Timeout" };
		SubprocessResult result = evaluateWithGhci(packagesToEnable, modulesToImport, true, expressionsToEvaluate.toArray(new String[0]), task, true);

		List<String> expectedValues = new ArrayList<>();

		for (String outputValue : result.stdOut().split(expectedValueSeparator)) {
			if (!outputValue.trim().isEmpty()) {
				expectedValues.add(outputValue);
			}
		}

		if (expectedValues.size() != wrappedFunctionCalls.size()) {
			throw new AssertionError(String.format("Expected values: %d, function calls: %d", expectedValues.size(), wrappedFunctionCalls.size()));
		}

		return expectedValues;
	}

	private static String wrapGhciExpressionInCatchAndTimeout(String expression) {
		// TODO@CHW: add setup option for timeout of single testcase
		return String.format("timeout 1000000 (catch (putStr (show (%s))) (putStr . (\"EXCEPTION: \" ++) . (let cutCallStack exc = take (fromMaybe (length exc) (findIndex (isPrefixOf \"\\nCallStack (from HasCallStack)\") (tails exc))) exc in cutCallStack) . show :: SomeException -> IO ())) >> return ()", expression);
	}

	public static String extractUnescapedGhciExpressionWrappedInCatchAndTimeout(String wrappedExpression) {
		// timeout\s+\d+\s+\(catch \(putStr \(show \((.*?)\)\)\)\s+\(putStr\s+\.\s+\("EXCEPTION: "\s+\+\+\)\s+\.\s+\(let cutCallStack exc.*? in cutCallStack\)\s+\.\s+show :: SomeException -> IO \(\)\)\) >> return \(\)
		String pattern = "timeout\\s+\\d+\\s+\\(catch \\(putStr \\(show \\((.*?)\\)\\)\\)\\s+\\(putStr\\s+\\.\\s+\\(\"EXCEPTION: \"\\s+\\+\\+\\)\\s+\\.\\s+\\(let cutCallStack exc.*? in cutCallStack\\)\\s+\\.\\s+show :: SomeException -> IO \\(\\)\\)\\) >> return \\(\\)";

		Pattern regex = Pattern.compile(pattern);
		Matcher matcher = regex.matcher(wrappedExpression);

		if (matcher.find()) {
			return matcher.group(1).replace("'\"'\"'", "'");
		} else {
			return null;
		}
	}

	public static String prettyPrintCyclicIntMappers(String functionCall) {
		return functionCall.replaceAll("\\(let cyclicIntMap.*? let randomFunction.*? in randomFunction\\)", "<random function>");
	}

	private static class HaskellClassifiedIdentifiers {
		private final List<HaskellClass> classes = new ArrayList<>();
		private final List<HaskellNewtypeOrData> newtypesAndDatas = new ArrayList<>();
		private final List<HaskellFunction> functions = new ArrayList<>();

		private void addClass(String hsClass) {
			classes.add(new HaskellClass(hsClass));
		}

		private void addNewtypeOrData(String hsNewtypeOrData) {
			newtypesAndDatas.add(new HaskellNewtypeOrData(hsNewtypeOrData));
		}

		private void addFunction(String hsFunction) {
			functions.add(new HaskellFunction(hsFunction));
		}

		private List<HaskellClass> getClasses() {
			return classes;
		}

		private List<HaskellNewtypeOrData> getNewtypesAndDatas() {
			return newtypesAndDatas;
		}

		private List<HaskellFunction> getFunctions() {
			return functions;
		}
	}

	private record HaskellClass(String hsClass) {
		private HaskellClass {
			if (!hsClass.startsWith("class") || !hsClass.contains("where")) {
				throw new IllegalArgumentException("Invalid class definition: " + hsClass);
			}
		}
	}

	private static class HaskellFunction {
		private final String name;
		private final String typeSignature;

		private HaskellFunction(String hsFunction) {
			if (!hsFunction.contains("::")) {
				throw new IllegalArgumentException("Invalid function definition: " + hsFunction);
			}

			String[] parts = hsFunction.split("::", 2);
			this.name = parts[0].trim();
			this.typeSignature = parts[1].trim();
		}

		private String getName() {
			return name;
		}

		private String getTypeSignature() {
			return typeSignature;
		}
	}

	private static class HaskellNewtypeOrData {
		private final String typename;
		private final String typeDefinition;
		private final List<String> constructors = new ArrayList<>();
		private final String arbitraryInstance;

		private HaskellNewtypeOrData(String hsNewtypeOrData) {
			String normalizedInput = hsNewtypeOrData.replace("\n", " ").trim();

			Pattern typePattern = Pattern.compile("\\b(?:data|newtype)\\s+" + "(?<typename>[\\w\\s]+?)" + "\\s*=\\s*" + "(?<constructors>.*?)(?=\\s+deriving\\b|$)");

			Matcher matcher = typePattern.matcher(normalizedInput);
			if (matcher.find()) {
				this.typename = matcher.group("typename").trim().replace("\n", " ");
				this.typeDefinition = hsNewtypeOrData;

				Pattern namedConstructorPattern = Pattern.compile("(?<constr>\\b\\w+\\b)\\s*\\{\\s*(?<fields>.*?)\\s*}");

				for (String constructor : matcher.group("constructors").replace("\n", " ").trim().split("\\|")) {
					Matcher namedConstructorPatternMatch = namedConstructorPattern.matcher(constructor.trim());

					if (namedConstructorPatternMatch.find()) {
						String constr = namedConstructorPatternMatch.group("constr");
						String fields = namedConstructorPatternMatch.group("fields");
						List<String> fieldTypes = new ArrayList<>();

						for (String f : fields.split(",")) {
							String[] parts = f.split("::");
							if (parts.length == 2) {
								fieldTypes.add(parts[1].trim());
							}
						}
						constructors.add(constr + " " + String.join(" ", fieldTypes));
					} else {
						constructors.add(constructor.trim());
					}
				}

				this.arbitraryInstance = generateArbitraryInstance();
			} else {
				throw new IllegalArgumentException("Invalid newtype/data definition: " + hsNewtypeOrData);
			}
		}

		private String getArbitraryInstance() {
			return arbitraryInstance;
		}

		private String getTypename() {
			return typename;
		}

		private String getTypeDefinition() {
			return typeDefinition;
		}

		private String generateArbitraryInstance() {
			List<String> recursiveReturnExpressions = new ArrayList<>();
			List<String> nonrecursiveReturnExpressions = new ArrayList<>();

			for (String constructor : constructors) {
				List<String> parts = splitExceptBetweenParentheses(constructor.trim(), '(', ')', " "); // TOOD@CHW this line is not yet correct
				String constructorName = constructor.trim().split("\\s+")[0];
				List<String> constructorArgs = parts.subList(1, parts.size());

				StringBuilder returnExpression = new StringBuilder("return " + constructorName);

				int totalNumRecursiveCalls = 0;
				for (String arg : constructorArgs) {
					if (constructorArgIsRecursive(arg)) {
						totalNumRecursiveCalls++;
					}
				}

				int recursiveCallId = 0;
				for (String constructorArg : constructorArgs) {
					if (constructorArgIsRecursive(constructorArg)) {
						returnExpression.append(" <*> _gen (splitElements (n - 1) ").append(totalNumRecursiveCalls).append(" ").append(recursiveCallId).append(")");
						recursiveCallId++;
					} else {
						returnExpression.append(" <*> arbitrary");
					}
				}

				if (recursiveCallId > 0) {
					recursiveReturnExpressions.add(returnExpression.toString());
				} else {
					nonrecursiveReturnExpressions.add(returnExpression.toString());
				}
			}

			String[] typenameWithTypeVariables = typename.split(" ");
			List<String> typeVariables = Arrays.asList(typenameWithTypeVariables).subList(1, typenameWithTypeVariables.length);
			String constraint = typeVariables.isEmpty() ? "" : "(" + String.join(", ", typeVariables.stream().map(v -> "Arbitrary " + v).toList()) + ") => ";

			List<String> freqTuples = getFreqTuples(recursiveReturnExpressions, nonrecursiveReturnExpressions);

			return String.format("""
					instance %sArbitrary (%s) where
					  arbitrary = sized _gen
					    where
					      _gen n
					        | n > 10 = _gen 10
					        | n > 0 && %s = frequency [%s]
					        | otherwise = oneof [%s]
					        where
					          splitElements numElements numRecursiveCalls recursiveCallId =
					            div numElements numRecursiveCalls + intDivRoundingCompensation
					              where
					                intDivRoundingCompensation =
					                  if recursiveCallId < mod numElements numRecursiveCalls then 1 else 0
					""", constraint, typename, recursiveReturnExpressions.isEmpty() ? "False" : "True", String.join(", ", freqTuples), String.join(", ", nonrecursiveReturnExpressions));
		}

		private static List<String> getFreqTuples(List<String> recursiveReturnExpressions, List<String> nonrecursiveReturnExpressions) {
			int recursiveProb = recursiveReturnExpressions.isEmpty() ? 0 : (int) ceil(82.0 / recursiveReturnExpressions.size());
			int nonrecursiveProb = nonrecursiveReturnExpressions.isEmpty() ? 0 : (int) ceil(18.0 / nonrecursiveReturnExpressions.size());

			List<String> freqTuples = new ArrayList<>();
			for (String r : recursiveReturnExpressions)
				freqTuples.add("(" + recursiveProb + ", " + r + ")");
			for (String n : nonrecursiveReturnExpressions)
				freqTuples.add("(" + nonrecursiveProb + ", " + n + ")");
			return freqTuples;
		}

		private boolean constructorArgIsRecursive(String arg) {
			arg = arg.trim();
			if (arg.startsWith("(") && arg.endsWith(")")) {
				return arg.equals("(" + this.typename + ")");
			} else {
				return arg.equals(this.typename);
			}
		}
	}

	private enum HaskellPrimitiveType {
		Integer, Int, Float, Double, Rational, Bool, Char, String
	}

	private enum HaskellConstrainedPrimitiveType {
		Char_OnlySafeAscii, String_OnlySafeAscii
		// TODO@CHW more ideas: Int_OnlyPositive, Float_OnlyPositive, Double_OnlyPositive
	}

	private static List<String> splitExceptBetweenParentheses(String expression, char openingParenthesis, char closingParenthesis, String splitAt) {
		List<String> tokens = new ArrayList<>();
		int parenthesisDepth = 0;
		StringBuilder currentToken = new StringBuilder();

		int index = 0;
		while (index < expression.length()) {
			char ch = expression.charAt(index);

			if (ch == openingParenthesis) {
				parenthesisDepth++;
				currentToken.append(ch);
			} else if (ch == closingParenthesis) {
				parenthesisDepth--;
				currentToken.append(ch);
			} else if (expression.startsWith(splitAt, index) && parenthesisDepth == 0) {
				tokens.add(currentToken.toString().trim());
				currentToken.setLength(0);
				index += splitAt.length() - 1; // skip already-processed characters
			} else {
				currentToken.append(ch);
			}
			index++;
		}

		tokens.add(currentToken.toString().trim());
		return tokens;
	}
}
