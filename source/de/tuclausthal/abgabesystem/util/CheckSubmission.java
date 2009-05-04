package de.tuclausthal.abgabesystem.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TestResultDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.JUnitTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.RegExpTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.TestResult;

public class CheckSubmission {
	private Submission submission;

	public CheckSubmission(Submission submission) {
		this.submission = submission;
	}

	public void check(HttpServletResponse response) throws IOException {
		Task task = submission.getTask();
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
		File path = new File("c:/abgabesystem/" + task.getLecture().getId() + "/" + task.getTaskid() + "/" + submission.getSubmissionid() + "/");
		if (path.exists() == false) {
			path.mkdirs();
		}
		// compile test
		// http://forums.java.net/jive/message.jspa?messageID=325269
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		/*StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null);
		Iterable codeObjecten = sjfm.getJavaFileObjects(uploadedFile);
		jc.getTask(null, sjfm, null, null, null, codeObjecten).call();
		sjfm.close();*/
		try {
			ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

			List<String> javaFiles = new LinkedList<String>();
			for (File javaFile : path.getAbsoluteFile().listFiles()) {
				if (javaFile.getName().endsWith(".java")) {
					javaFiles.add(javaFile.getAbsolutePath());
				}
			}
			int a = jc.run(null, null, errorOutputStream, javaFiles.toArray(new String[] {}));
			submission.setCompiles(a == 0);
			submission.setStderr(errorOutputStream.toString());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "System.getProperty(\"java.home\") should point to a jre in a jdk directory");
			return;
		}

		// Test exists
		if (task.getTest() != null) {
			// check what kind of test it is
			List<String> params = new LinkedList<String>();
			params.add("java");
			// TODO: add security here
			//params.add("-Djava.security.manager");
			//params.add("-Djava.security.policy=myPol.policy");
			if (task.getTest() instanceof JUnitTest) {
				// TODO: only win atm
				params.add("-cp");
				params.add("..\\junittest.jar;c:\\junit.jar;.");
				params.add("junit.textui.TestRunner");
				params.add("AllTests");
			} else if (task.getTest() instanceof RegExpTest) {
				RegExpTest regExpTest = (RegExpTest) task.getTest();
				params.add(regExpTest.getMainClass());
				params.addAll(Arrays.asList(regExpTest.getCommandLineParameter().split(" ")));
			} else {
				// TODO: throw error!
			}
			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(path);
			Process process = pb.start();
			CheckThread checkTread = new CheckThread(process);
			checkTread.run();
			int exitValue = -1;
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
			BufferedReader testErrorInputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
			TestResultDAOIf testResultDAO = DAOFactory.TestResultDAOIf();
			TestResult testResult = testResultDAO.createTestResult(submission);
			testResult.setPassedTest((exitValue == 0));
			String testError = "";
			String line;
			while ((line = testErrorInputStream.readLine()) != null) {
				testError = testError.concat(line + "\n");
			}
			if (task.getTest() instanceof RegExpTest) {
				System.out.println(((RegExpTest) task.getTest()).getRegularExpression());
				System.out.println(testError);
				Pattern testPattern = Pattern.compile(((RegExpTest) task.getTest()).getRegularExpression());
				Matcher testMatcher = testPattern.matcher(testError.trim());
				if (!testMatcher.matches()) {
					testError = "regexp doesn't match. Output follows:\n" + testError;
					testResult.setPassedTest(false);
				}
			}
			testResult.setTestOutput(testError);
			submissionDAO.saveSubmission(submission);
		}
	}
}
