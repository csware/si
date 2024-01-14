/*
 * Copyright 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;

import org.htmlunit.ElementNotFoundException;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.TextPage;
import org.htmlunit.UnexpectedPage;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlCheckBoxInput;
import org.htmlunit.html.HtmlFileInput;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlLabel;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlPasswordInput;
import org.htmlunit.html.HtmlSubmitInput;
import org.htmlunit.html.HtmlTable;
import org.htmlunit.html.HtmlTableRow;
import org.htmlunit.html.HtmlTextInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestCount;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestCount_;
import de.tuclausthal.submissioninterface.util.Util;

@EnabledIfEnvironmentVariable(named = "GATE_WEB_TESTS", matches = "http.+")
class WebClientTest {
	private final static String WEBROOT = System.getenv("GATE_WEB_TESTS");
	private WebClient webClient;

	@BeforeEach
	public void createWebClient() {
		webClient = new WebClient();
		webClient.setCssErrorHandler(new SilentCssErrorHandler());
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
	}

	@AfterEach
	public void closeWebClient() {
		webClient.close();
	}

	@Nested
	class LoginAsStudentUser0 extends BasicTest {
		final private String user = "user0";

		@BeforeEach
		void login() throws Exception {
			final HtmlPage loginPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/Overview");
			assertEquals("GATE: Login erforderlich", loginPage.getTitleText());
			final HtmlForm form = loginPage.getFormByName("login");
			final HtmlTextInput username = form.getInputByName("username");
			final HtmlPasswordInput password = form.getInputByName("password");
			username.type(user);
			password.type("something");
			final HtmlPage overviewPage = form.getOneHtmlElementByAttribute("input", "type", "submit").click();
			assertEquals("GATE: Meine Veranstaltungen", overviewPage.getTitleText());
		}

		@GATEDBTest
		@Nested
		class SubscribeToLecture {
			private Lecture lecture;
			private int oldSemester;
			private Participation participation;

			@BeforeEach
			void subscribe() throws Exception {
				HtmlPage subscribePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubscribeToLecture");
				assertEquals("GATE: Veranstaltungen", subscribePage.getTitleText());
				assertTrue(subscribePage.asNormalizedText().contains("keine Veranstaltungen gefunden."), "keine Veranstaltungen gefunden.");

				lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
				participation = DAOFactory.ParticipationDAOIf(session).getParticipation(DAOFactory.UserDAOIf(session).getUserByUsername(user), lecture);
				oldSemester = lecture.getSemester();

				session.beginTransaction();
				lecture.setSemester(Util.getCurrentSemester());
				session.getTransaction().commit();

				subscribePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubscribeToLecture");
				assertEquals("GATE: Veranstaltungen", subscribePage.getTitleText());
				assertTrue(subscribePage.asNormalizedText().contains("Lecture 1"), "Lecture 1");
				assertEquals(1, subscribePage.getForms().size());
				final HtmlForm form = subscribePage.getForms().get(0);
				final HtmlPage lecturePage = form.getOneHtmlElementByAttribute("input", "type", "submit").click();
				assertEquals("GATE: Veranstaltung \"Lecture 1\"", lecturePage.getTitleText());

				participation = DAOFactory.ParticipationDAOIf(session).getParticipation(DAOFactory.UserDAOIf(session).getUserByUsername(user), lecture);
			}

			@AfterEach
			void resetSubscribe() {
				session.beginTransaction();
				session.refresh(lecture);
				lecture.setSemester(oldSemester);
				DAOFactory.SubmissionDAOIf(session).getAllSubmissions(participation).forEach(s -> session.remove(s));
				DAOFactory.ParticipationDAOIf(session).deleteParticipation(participation);
				session.getTransaction().commit();
			}

			@Test
			void changeGroup() throws Exception {
				HtmlPage lecturePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowLecture?lecture=1");
				assertEquals("GATE: Veranstaltung \"Lecture 1\"", lecturePage.getTitleText());
				assertTrue(lecturePage.asNormalizedText().contains("Sie sind derzeit in keiner Gruppe."), "Sie sind derzeit in keiner Gruppe.");
				assertEquals(2, lecturePage.getForms().get(0).getSelectByName("groupid").getOptionSize());

				lecturePage = lecturePage.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
				assertTrue(lecturePage.asNormalizedText().contains("Meine Gruppe: Group 3"), "Meine Gruppe: Group 3");
				assertEquals(1, lecturePage.getForms().get(0).getSelectByName("groupid").getOptionSize());

				lecturePage = lecturePage.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
				assertEquals("GATE: Veranstaltung \"Lecture 1\"", lecturePage.getTitleText());
				assertTrue(lecturePage.asNormalizedText().contains("Meine Gruppe: Group 4 (empty)"), "Meine Gruppe: Group 4 (empty)");

				lecturePage = lecturePage.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
				assertEquals("GATE: Veranstaltung \"Lecture 1\"", lecturePage.getTitleText());
				assertTrue(lecturePage.asNormalizedText().contains("Meine Gruppe: Group 3"), "Meine Gruppe: Group 3");
			}

			@ParameterizedTest
			@CsvSource({ "TriangleOutput.java, TriangleOutput.java", "TriangleOutputPackage.java, mypackage/TriangleOutput.java" })
			void uploadFile(final String filename, final String remoteFilename, @TempDir final File tmp) throws Exception {
				final HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=4");
				assertEquals("GATE: Aufgabe \"Something\"", taskPage.getTitleText());
				assertTrue(taskPage.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");

				final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=4");
				final HtmlForm submitForm = submitPage.getForms().get(0);
				final HtmlFileInput fileInput = submitForm.getInputByName("file");
				fileInput.setValue("TriangleOutput.java");
				final String fileContents = Util.loadFile(new File("src/test/resources", filename)).toString();
				fileInput.setData(fileContents.getBytes());
				final HtmlPage submittedPage = submitForm.getOneHtmlElementByAttribute("input", "type", "submit").click();
				final String pageContent = submittedPage.asNormalizedText();
				assertTrue(pageContent.contains("TriangleOutput.java"), "TriangleOutput.java");
				assertTrue(pageContent.contains("Abgabe bearbeiten/erweitern"), "Abgabe bearbeiten/erweitern");
				assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmission(DAOFactory.TaskDAOIf(session).getTask(4), participation.getUser()).getSubmitters().size());

				// retrieve file
				final HtmlAnchor viewFileLink = submittedPage.getFirstByXPath("//a[text()='" + remoteFilename + "']");
				final HtmlPage uploadedFile = viewFileLink.click();
				assertEquals(fileContents.trim(), uploadedFile.getElementById("fileContents").getTextContent().trim());

				// download file
				final UnexpectedPage downloadFilePage = webClient.getPage(uploadedFile.getUrl().toString() + "&download=true");
				final File tmpFile = new File(tmp, "tmpfile");
				try (InputStream is = downloadFilePage.getInputStream()) {
					Util.copyInputStreamAndClose(is, tmpFile);
				}
				assertEquals(fileContents, Util.loadFile(tmpFile).toString());

				// delete file
				final HtmlAnchor deleteLink = submittedPage.getFirstByXPath("//a[text()='löschen']");
				final HtmlPage deleteConfirm = deleteLink.click();
				assertEquals("GATE: Datei löschen", deleteConfirm.getTitleText());
				assertTrue(deleteConfirm.asNormalizedText().contains("Datei \"" + remoteFilename + "\" löschen"), "Datei \"" + remoteFilename + "\" löschen");
				final HtmlPage afterDelete = deleteConfirm.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
				assertTrue(afterDelete.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");
			}

			@Nested
			class Task1DynamicTaskTimeChecks {
				private Task task;
				private ZonedDateTime oldDeadline;

				@BeforeEach
				void rememberOldData() {
					task = DAOFactory.TaskDAOIf(session).getTask(1);
					oldDeadline = task.getDeadline();
				}

				@AfterEach
				void reset() {
					session.beginTransaction();
					session.refresh(task);
					task.setDeadline(oldDeadline);
					{
						CriteriaBuilder builder = session.getCriteriaBuilder();
						CriteriaDelete<TestCount> criteria = builder.createCriteriaDelete(TestCount.class);
						Root<TestCount> root = criteria.from(TestCount.class);
						criteria.where(builder.equal(root.get(TestCount_.user), participation.getUser()));
						session.createMutationQuery(criteria).executeUpdate();
					}
					session.getTransaction().commit();
				}

				@Test
				void testDynamicNumbersTask(@TempDir final File tmp) throws Exception {
					HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=" + task.getTaskid());
					assertEquals("GATE: Aufgabe \"Dynamic Task binary numbers\"", taskPage.getTitleText());
					assertTrue(taskPage.asNormalizedText().contains("Keine Abgabe mehr möglich."), "Keine Abgabe mehr möglich.");

					session.beginTransaction();
					task.setDeadline(ZonedDateTime.now().plusDays(1));
					session.getTransaction().commit();

					taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=" + task.getTaskid());
					assertTrue(taskPage.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");

					final Pattern p = Pattern.compile(".*Berechnen Sie die Dezimaldarstellung des Binär-Wertes ([01]+)\\..*", Pattern.DOTALL);
					final Matcher matcher = p.matcher(taskPage.asNormalizedText());
					assertTrue(matcher.matches(), "Berechnen Sie die Dezimaldarstellung des Binär-Wertes");
					final String binaryNumber = matcher.group(1);

					final String randomString = String.valueOf(new Random().nextLong());
					final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=" + task.getTaskid());
					final HtmlForm submitForm = submitPage.getForms().get(0);
					final String dynamicResult = "some" + randomString + "thing";
					final String textSolution = "some arguments for the solution\n" + randomString + "\n" + binaryNumber;
					submitForm.getInputByName("dynamicresult0").setValue(dynamicResult);
					submitForm.getTextAreaByName("textsolution").setText(textSolution);

					final HtmlPage submittedPage = submitForm.getOneHtmlElementByAttribute("input", "type", "submit").click();
					assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser()).getSubmitters().size());
					assertEquals(List.of(dynamicResult), DAOFactory.ResultDAOIf(session).getResultsForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser())));
					final String submittedPageContent = submittedPage.asNormalizedText();
					assertTrue(submittedPageContent.contains("Berechnen Sie die Dezimaldarstellung des Binär-Wertes " + binaryNumber + "."), "Berechnen Sie die Dezimaldarstellung des Binär-Wertes " + binaryNumber + ".");
					assertTrue(submittedPageContent.contains("Lösung der Umrechnung: " + dynamicResult), "Lösung der Umrechnung: " + dynamicResult);
					assertTrue(submittedPageContent.contains("textloesung.txt"), "textloesung.txt");
					assertTrue(submittedPageContent.contains("Abgabe bearbeiten/erweitern"), "Abgabe bearbeiten/erweitern");

					// retrieve file
					final HtmlAnchor viewFileLink = submittedPage.getFirstByXPath("//a[text()='textloesung.txt']");
					final HtmlPage uploadedFile = viewFileLink.click();
					assertEquals(textSolution, uploadedFile.getElementById("fileContents").getTextContent().trim());

					// download file
					final TextPage downloadFilePage = webClient.getPage(uploadedFile.getUrl().toString() + "&download=true");
					final File tmpFile = new File(tmp, "tmpfile");
					Util.copyInputStreamAndClose(new ByteArrayInputStream(downloadFilePage.getContent().getBytes()), tmpFile);
					assertEquals(textSolution, Util.loadFile(tmpFile).toString());

					// delete file
					final HtmlAnchor deleteLink = submittedPage.getFirstByXPath("//a[text()='löschen']");
					final HtmlPage deleteConfirm = deleteLink.click();
					assertEquals("GATE: Datei löschen", deleteConfirm.getTitleText());
					final HtmlPage afterDelete = deleteConfirm.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
					final String afterDeletePageContent = afterDelete.asNormalizedText();
					assertTrue(afterDelete.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");
					assertTrue(afterDeletePageContent.contains("Berechnen Sie die Dezimaldarstellung des Binär-Wertes " + binaryNumber + "."), "Berechnen Sie die Dezimaldarstellung des Binär-Wertes " + binaryNumber + ".");
				}
			}

			@Nested
			class Task2MCTaskTimeChecks {
				private Task task;
				private ZonedDateTime oldDeadline;

				@BeforeEach
				void rememberOldData() {
					task = DAOFactory.TaskDAOIf(session).getTask(2);
					oldDeadline = task.getDeadline();
				}

				@AfterEach
				void reset() {
					session.beginTransaction();
					session.refresh(task);
					task.setDeadline(oldDeadline);
					session.getTransaction().commit();
				}

				@Test
				void testMCTask(@TempDir final File tmp) throws Exception {
					HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=" + task.getTaskid());
					assertEquals("GATE: Aufgabe \"MC Task\"", taskPage.getTitleText());
					assertTrue(taskPage.asNormalizedText().contains("Keine Abgabe mehr möglich."), "Keine Abgabe mehr möglich.");
					assertEquals(0, taskPage.getByXPath("//input[@type='checkbox']").size());

					session.beginTransaction();
					task.setDeadline(ZonedDateTime.now().plusDays(1));
					session.getTransaction().commit();

					taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=" + task.getTaskid());
					assertTrue(taskPage.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");

					{
						final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=" + task.getTaskid());
						final HtmlForm submitForm = submitPage.getForms().get(0);
						for (final HtmlCheckBoxInput checkbox : submitForm.<HtmlCheckBoxInput> getByXPath(".//input[@type='checkbox']")) {
							assertFalse(checkbox.isDisabled());
							assertFalse(checkbox.isChecked());
						}
						final HtmlLabel label = submitForm.getFirstByXPath(".//label[text()='Correct']");
						final HtmlCheckBoxInput correctCheckbox = (HtmlCheckBoxInput) label.getLabeledElement();
						correctCheckbox.setChecked(true);
						taskPage = submitForm.getOneHtmlElementByAttribute("input", "type", "submit").click();
						assertEquals(List.of("3"), DAOFactory.ResultDAOIf(session).getResultsForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser())));
						assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser()).getSubmitters().size());
					}

					{
						final HtmlLabel label = taskPage.getFirstByXPath("//li/label[text()='Correct']");
						final HtmlCheckBoxInput correctCheckbox = (HtmlCheckBoxInput) label.getLabeledElement();
						assertTrue(correctCheckbox.isChecked());
						for (final HtmlCheckBoxInput checkbox : taskPage.<HtmlCheckBoxInput> getByXPath("//input[@type='checkbox']")) {
							assertTrue(checkbox.isDisabled());
							if (checkbox.equals(correctCheckbox)) {
								continue;
							}
							assertFalse(checkbox.isChecked());
						}
					}

					{
						final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=" + task.getTaskid());
						final HtmlForm submitForm = submitPage.getForms().get(0);
						final HtmlLabel correctLabel = submitForm.getFirstByXPath(".//label[text()='Correct']");
						final HtmlCheckBoxInput correctCheckbox = (HtmlCheckBoxInput) correctLabel.getLabeledElement();
						assertTrue(correctCheckbox.isChecked());
						for (final HtmlCheckBoxInput checkbox : submitForm.<HtmlCheckBoxInput> getByXPath(".//input[@type='checkbox']")) {
							assertFalse(checkbox.isDisabled());
							if (checkbox.equals(correctCheckbox)) {
								continue;
							}
							assertFalse(checkbox.isChecked());
						}
						correctCheckbox.setChecked(false);
						final HtmlLabel label1 = submitForm.getFirstByXPath(".//label[text()='Wrong 1']");
						((HtmlCheckBoxInput) label1.getLabeledElement()).setChecked(true);
						final HtmlLabel label2 = submitForm.getFirstByXPath(".//label[text()='Wrong 3']");
						((HtmlCheckBoxInput) label2.getLabeledElement()).setChecked(true);
						taskPage = submitForm.getOneHtmlElementByAttribute("input", "type", "submit").click();
						assertEquals(List.of("1", "4"), DAOFactory.ResultDAOIf(session).getResultsForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser())));
						assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser()).getSubmitters().size());
					}

					{
						final HtmlLabel label1 = taskPage.getFirstByXPath("//li/label[text()='Wrong 1']");
						assertTrue(((HtmlCheckBoxInput) label1.getLabeledElement()).isChecked());
						final HtmlLabel label2 = taskPage.getFirstByXPath("//li/label[text()='Wrong 3']");
						assertTrue(((HtmlCheckBoxInput) label2.getLabeledElement()).isChecked());
						for (final HtmlCheckBoxInput checkbox : taskPage.<HtmlCheckBoxInput> getByXPath(".//input[@type='checkbox']")) {
							assertTrue(checkbox.isDisabled());
							if (checkbox.equals(label1.getLabeledElement()) || checkbox.equals(label2.getLabeledElement())) {
								continue;
							}
							assertFalse(checkbox.isChecked());
						}
					}

					{
						final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=" + task.getTaskid());
						final HtmlForm submitForm = submitPage.getForms().get(0);
						for (final HtmlCheckBoxInput checkbox : submitForm.<HtmlCheckBoxInput> getByXPath(".//input[@type='checkbox']")) {
							assertFalse(checkbox.isDisabled());
							checkbox.setChecked(false);
						}
						taskPage = submitForm.getOneHtmlElementByAttribute("input", "type", "submit").click();
						assertEquals(List.of(), DAOFactory.ResultDAOIf(session).getResultsForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser())));
					}

					for (final HtmlCheckBoxInput checkbox : taskPage.<HtmlCheckBoxInput> getByXPath("//input[@type='checkbox']")) {
						assertFalse(checkbox.isChecked());
					}
				}
			}

			@Nested
			class Task3FileUploadTimeChecks {
				private Task task;
				private ZonedDateTime oldDeadline;

				@BeforeEach
				void RememberOldDeadline() {
					task = DAOFactory.TaskDAOIf(session).getTask(3);
					oldDeadline = task.getDeadline();
				}

				@AfterEach
				void reset() {
					session.beginTransaction();
					session.refresh(task);
					task.setDeadline(oldDeadline);
					{
						CriteriaBuilder builder = session.getCriteriaBuilder();
						CriteriaDelete<TestCount> criteria = builder.createCriteriaDelete(TestCount.class);
						Root<TestCount> root = criteria.from(TestCount.class);
						criteria.where(builder.equal(root.get(TestCount_.user), participation.getUser()));
						session.createMutationQuery(criteria).executeUpdate();
					}
					session.getTransaction().commit();
				}

				@Test
				void testUploadFileAndTest() throws Exception {
					HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=" + task.getTaskid());
					assertEquals("GATE: Aufgabe \"Hello World\"", taskPage.getTitleText());
					assertTrue(taskPage.asNormalizedText().contains("Keine Abgabe mehr möglich."), "Keine Abgabe mehr möglich.");

					session.beginTransaction();
					task.setDeadline(ZonedDateTime.now().plusDays(1));
					session.getTransaction().commit();

					taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=3");
					assertTrue(taskPage.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");

					final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=" + task.getTaskid());
					final HtmlForm submitForm = submitPage.getForms().get(0);
					final HtmlFileInput fileInput = submitForm.getInputByName("file");
					final HtmlSubmitInput submit = submitForm.getOneHtmlElementByAttribute("input", "type", "submit");
					fileInput.setValue("HelloWorld.java");
					fileInput.setData("public class HelloWorld {\n	public static void main(String[] args) {\n		Systemout.println(\"Hello World!\");\n	}\n}".getBytes());

					HtmlPage submittedPage = submit.click();
					final String pageContent = submittedPage.asNormalizedText();
					assertTrue(pageContent.contains("HelloWorld.java"), "HelloWorld.java");
					assertTrue(pageContent.contains("Abgabe bearbeiten/erweitern"), "Abgabe bearbeiten/erweitern");
					assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser()).getSubmitters().size());

					// 3 different tests should be available
					assertEquals(3, submittedPage.getForms().size());
					HtmlPage testResult = submittedPage.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
					while ("GATE: Testen...".equals(testResult.getTitleText())) {
						Thread.sleep(250);
						final HtmlAnchor followLink = testResult.getFirstByXPath("//a[text()='hier']");
						testResult = followLink.click();
					}
					assertEquals("GATE: Testergebnis", testResult.getTitleText());
					assertTrue(testResult.asNormalizedText().contains("Bestanden: nein"), "Bestanden: nein");

					// new upload
					fileInput.reset();
					fileInput.setValue("HelloWorld.java");
					fileInput.setData("public class HelloWorld {\n	public static void main(String[] args) {\n		System.out.println(\"Hello World!\");\n	}\n}".getBytes());
					submittedPage = submit.click();
					assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmission(task, participation.getUser()).getSubmitters().size());

					// 3 different tests should still be available
					assertEquals(3, submittedPage.getForms().size());
					testResult = submittedPage.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
					while ("GATE: Testen...".equals(testResult.getTitleText())) {
						Thread.sleep(250);
						final HtmlAnchor followLink = testResult.getFirstByXPath("//a[text()='hier']");
						testResult = followLink.click();
					}
					assertEquals("GATE: Testergebnis", testResult.getTitleText());
					assertTrue(testResult.asNormalizedText().contains("Bestanden: ja"), "Bestanden: ja");

					// try get get third test result
					testResult = submittedPage.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
					assertEquals("GATE: Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.", testResult.getTitleText());

					// now the syntax test should be gone
					submittedPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=" + task.getTaskid());
					assertEquals(2, submittedPage.getForms().size());

					// delete file
					HtmlAnchor deleteLink = submittedPage.getFirstByXPath("//a[text()='löschen']");
					final HtmlPage deleteConfirm = deleteLink.click();
					assertEquals("GATE: Datei löschen", deleteConfirm.getTitleText());
					final HtmlPage afterDelete = deleteConfirm.getForms().get(0).getOneHtmlElementByAttribute("input", "type", "submit").click();
					assertTrue(afterDelete.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");
				}
			}

			@Nested
			class Task4FileUploadTimeChecks extends BasicTest {
				private Task task;
				private ZonedDateTime oldDeadline;

				@BeforeEach
				void RememberOldDeadline() {
					task = DAOFactory.TaskDAOIf(session).getTask(4);
					oldDeadline = task.getDeadline();
				}

				@AfterEach
				void ResetOldDeadline() {
					session.beginTransaction();
					session.refresh(task);
					task.setDeadline(oldDeadline);
					session.getTransaction().commit();
				}

				@Test
				void testUploadFileAfterDeadline() throws Exception {
					final HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=4");
					assertTrue(taskPage.asNormalizedText().contains("Abgabe starten"), "Abgabe starten");

					final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=4");
					final HtmlForm submitForm = submitPage.getForms().get(0);
					final HtmlFileInput fileInput = submitForm.getInputByName("file");
					fileInput.setFiles(new File("src/test/resources/TriangleOutput.java"));

					session.beginTransaction();
					task.setDeadline(ZonedDateTime.now());
					session.getTransaction().commit();

					final HtmlPage submittedPage = submitForm.getOneHtmlElementByAttribute("input", "type", "submit").click();
					final String pageContent = submittedPage.asNormalizedText();
					assertTrue(pageContent.contains("Abgabe nicht mehr möglich."), "Abgabe nicht mehr möglich.");
				}
			}
		}
	}

	@Nested
	class LoginAsStudentUser1 {
		final private String user = "user1";

		@BeforeEach
		void login() throws Exception {
			final HtmlPage loginPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/Overview");
			assertEquals("GATE: Login erforderlich", loginPage.getTitleText());
			final HtmlForm form = loginPage.getFormByName("login");
			final HtmlTextInput username = form.getInputByName("username");
			final HtmlPasswordInput password = form.getInputByName("password");
			username.type(user);
			password.type("something");
			final HtmlPage overviewPage = form.getOneHtmlElementByAttribute("input", "type", "submit").click();
			assertEquals("GATE: Meine Veranstaltungen", overviewPage.getTitleText());
		}

		@Test
		void NoAdmin() throws Exception {
			final HtmlPage overviewPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/Overview");
			assertThrows(ElementNotFoundException.class, () -> overviewPage.getAnchorByText("Admin-Menü"));

			Exception exception = assertThrows(FailingHttpStatusCodeException.class, () -> webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/AdminMenue"));
			assertTrue(exception.getMessage().startsWith("403 "), "403 ");

			Exception exception2 = assertThrows(FailingHttpStatusCodeException.class, () -> webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SelfTest"));
			assertTrue(exception2.getMessage().startsWith("403 "), "403 ");
		}

		@Test
		void lectureView() throws Exception {
			final HtmlPage lecturePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowLecture?lecture=1");
			assertEquals("GATE: Veranstaltung \"Lecture 1\"", lecturePage.getTitleText());
			final String pageText = lecturePage.asNormalizedText();
			assertTrue(pageText.contains("Meine Gruppe: Group 1 (fixed)"), "Meine Gruppe: Group 1 (fixed)");
			assertEquals(0, lecturePage.getForms().size());
			assertTrue(!pageText.contains("Teilnehmende"), "Teilnehmende");
			assertTrue(pageText.contains("Dynamic Task binary numbers	1	1"), "Dynamic Task binary numbers	1	1");
			assertTrue(pageText.contains("MC Task	2	0, nicht abgenommen"), "MC Task	2	0, nicht abgenommen");
			assertTrue(pageText.contains("Hello World	1,5	1,5"), "Hello World	1,5	1,5");
			assertTrue(pageText.contains("Something	5	(noch) nicht bearbeitet"), "Something	5	(noch) nicht bearbeitet");
			assertTrue(pageText.contains("Gesamt:	9,5	2,5"), "Gesamt:	9,5	2,5");

		}

		@Test
		void uploadFileToClosedTask() throws Exception {
			final HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=3");
			final String taskPageContent = taskPage.asNormalizedText();
			assertTrue(taskPageContent.contains("Keine Abgabe mehr möglich."), "Keine Abgabe mehr möglich.");
			assertFalse(taskPageContent.contains("Abgabe starten"), "Abgabe starten");
			assertFalse(taskPageContent.contains("Abgabe bearbeiten/erweitern"), "Abgabe bearbeiten/erweitern");

			final HtmlPage submitPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubmitSolution?taskid=3");
			assertTrue(submitPage.asNormalizedText().contains("Abgabe nicht mehr möglich"), "Abgabe nicht mehr möglich");
		}

		@Test
		void deleteFileOfClosedTask() throws Exception {
			final HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/DeleteFile/HelloWorld.java?sid=10");
			assertEquals("GATE: Es sind keine Veränderungen an dieser Abgabe mehr möglich.", taskPage.getTitleText());
		}

		@Test
		void subscribeToLectureEmpty() throws Exception {
			final HtmlPage subscribePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SubscribeToLecture");
			assertEquals("GATE: Veranstaltungen", subscribePage.getTitleText());
			assertTrue(subscribePage.asNormalizedText().contains("keine Veranstaltungen gefunden."), "keine Veranstaltungen gefunden.");
		}

		@Test
		void cannotAccessFileOfOtherSubmission() throws Exception {
			Exception exception = assertThrows(FailingHttpStatusCodeException.class, () -> webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowFile/HelloWorld.java?sid=7"));
			assertTrue(exception.getMessage().startsWith("403 "), "403 ");
		}

		@Test
		void cannotShowSubmission() throws Exception {
			Exception exception = assertThrows(FailingHttpStatusCodeException.class, () -> webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowSubmission?sid=10"));
			assertTrue(exception.getMessage().startsWith("403 "), "403 ");
		}

		@Test
		void mcTaskCorrectSelected() throws Exception {
			final HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=2");
			final HtmlLabel label = taskPage.getFirstByXPath("//li/label[text()='Correct']");
			final HtmlCheckBoxInput correctCheckbox = (HtmlCheckBoxInput) label.getLabeledElement();
			assertTrue(correctCheckbox.isChecked());
			for (final HtmlCheckBoxInput checkbox : taskPage.<HtmlCheckBoxInput> getByXPath("//input[@type='checkbox']")) {
				assertTrue(checkbox.isDisabled());
				if (checkbox.equals(correctCheckbox)) {
					continue;
				}
				assertFalse(checkbox.isChecked());
			}
		}
	}

	@Nested
	class LoginAsTutorUser9 {
		final private String user = "user9";

		@BeforeEach
		void login() throws Exception {
			final HtmlPage loginPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/Overview");
			assertEquals("GATE: Login erforderlich", loginPage.getTitleText());
			final HtmlForm form = loginPage.getFormByName("login");
			final HtmlTextInput username = form.getInputByName("username");
			final HtmlPasswordInput password = form.getInputByName("password");
			username.type(user);
			password.type("something");
			final HtmlPage overviewPage = form.getOneHtmlElementByAttribute("input", "type", "submit").click();
			assertEquals("GATE: Meine Veranstaltungen", overviewPage.getTitleText());
		}

		@Test
		void lectureTutorView() throws Exception {
			final HtmlPage lecturePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowLecture?lecture=1");
			assertEquals("GATE: Veranstaltung \"Lecture 1\"", lecturePage.getTitleText());
			final String pageText = lecturePage.asNormalizedText();
			assertTrue(pageText.contains("Teilnehmende"), "Teilnehmende");
			assertTrue(pageText.contains("Studierende: 8; Gesamtdurchschnitt: 0,93"), "Studierende: 8; Gesamtdurchschnitt: 0,93");
		}

		@Test
		void taskView() throws Exception {
			final HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=3");
			assertEquals("GATE: Aufgabe \"Hello World\"", taskPage.getTitleText());
			final String pageText = taskPage.asNormalizedText();
			assertTrue(pageText.contains("Teilnehmende ohne Gruppenzugehörigkeit"), "Teilnehmende ohne Gruppenzugehörigkeit");
		}

		@Test
		void showSubmission() throws Exception {
			final HtmlPage taskPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowSubmission?sid=10");
			assertEquals("GATE: Abgabe von \"Lastname1, Firstname1; Lastname2, Firstname2\"", taskPage.getTitleText());
		}

		@Test
		void downloadTaskGroupAsZipUnGrouped() throws Exception {
			final Task task = new Task();
			task.setArchiveFilenameRegexp(".+");
			final UnexpectedPage downloadFilePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/DownloadSubmissionsByGroup?taskid=3");
			List<String> files = new ArrayList<>();
			try (InputStream is = downloadFilePage.getInputStream(); ZipInputStream zipInputStream = new ZipInputStream(is)) {
				ZipEntry zipEntry;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					files.add(zipEntry.getName());
				}
			}
			assertEquals(List.of("3 Lastname6, Firstname6" + System.getProperty("file.separator") + "HelloWorld.java", "7 Lastname7, Firstname7" + System.getProperty("file.separator") + "HelloWorld.java"), files);
		}

		@Test
		void downloadTaskGroupAsZipGroup2() throws Exception {
			final Task task = new Task();
			task.setArchiveFilenameRegexp(".+");
			final UnexpectedPage downloadFilePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/DownloadSubmissionsByGroup?taskid=3&groupid=2");
			List<String> files = new ArrayList<>();
			try (InputStream is = downloadFilePage.getInputStream(); ZipInputStream zipInputStream = new ZipInputStream(is)) {
				ZipEntry zipEntry;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					files.add(zipEntry.getName());
				}
			}
			assertEquals(List.of("12 Lastname3, Firstname3; Lastname4, Firstname4" + System.getProperty("file.separator") + "somepackage" + System.getProperty("file.separator") + "HelloWorld.java"), files);
		}

		@Test
		void downloadSubmissionAsZip(@TempDir final File tmp) throws Exception {
			final Task task = new Task();
			task.setArchiveFilenameRegexp(".+");
			final UnexpectedPage downloadFilePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/DownloadAsZip?sid=12");
			List<String> files = new ArrayList<>();
			try (InputStream is = downloadFilePage.getInputStream(); ZipInputStream zipInputStream = new ZipInputStream(is)) {
				ZipEntry zipEntry;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					files.add(zipEntry.getName());
				}
			}
			assertEquals(List.of("somepackage" + System.getProperty("file.separator") + "HelloWorld.java"), files);
		}

		@Test
		void accessFileSid10(@TempDir final File tmp) throws Exception {
			final HtmlPage uploadedFile = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowFile/HelloWorld.java?sid=10");
			final String fileContents = Util.loadFile(new File("src/test/resources/lecture1/3/10/HelloWorld.java")).toString();
			assertEquals(fileContents.trim(), uploadedFile.getElementById("fileContents").getTextContent().trim());

			// download file
			final UnexpectedPage downloadFilePage = webClient.getPage(uploadedFile.getUrl().toString() + "&download=true");
			final File tmpFile = new File(tmp, "tmpfile");
			try (InputStream is = downloadFilePage.getInputStream()) {
				Util.copyInputStreamAndClose(is, tmpFile);
			}
			assertEquals(fileContents, Util.loadFile(tmpFile).toString());
		}

		@Test
		void accessFileSid8(@TempDir final File tmp) throws Exception {
			final HtmlPage uploadedFile = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowFile/textloesung.txt?sid=8");
			final String fileContents = Util.loadFile(new File("src/test/resources/lecture1/1/8/textloesung.txt")).toString();
			assertEquals(fileContents.trim(), uploadedFile.getElementById("fileContents").getTextContent().trim());

			// download file
			final TextPage downloadFilePage = webClient.getPage(uploadedFile.getUrl().toString() + "&download=true");
			final File tmpFile = new File(tmp, "tmpfile");
			Util.copyInputStreamAndClose(new ByteArrayInputStream(downloadFilePage.getContent().getBytes()), tmpFile);
			assertEquals(fileContents, Util.loadFile(tmpFile).toString());
		}

		@Test
		void showUserId8() throws Exception {
			final HtmlPage userPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowUser?uid=8");
			assertEquals("GATE: BenutzerIn \"Lastname6, Firstname6\"", userPage.getTitleText());
			final String pageText = userPage.asNormalizedText();
			assertTrue(pageText.contains("Lecture 1"), "Lecture 1");
			assertTrue(!pageText.contains("Lecture 2"), "Lecture 2");
		}

		@Test
		void cannotAccessUserId2() throws Exception {
			Exception exception = assertThrows(FailingHttpStatusCodeException.class, () -> webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowUser?uid=2"));
			assertTrue(exception.getMessage().startsWith("403 "), "403 ");
		}

		@Nested
		class PointCalculation {
			@Test
			void fullList() throws Exception {
				final HtmlPage page = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowLecture?lecture=1&show=list");
				assertEquals("GATE: Gesamtübersicht", page.getTitleText());

				final HtmlTable table = page.getFirstByXPath("//table");
				var bodies = table.getBodies();
				assertEquals(1, bodies.size());
				final List<HtmlTableRow> rows = bodies.get(0).getRows();
				assertEquals(8, rows.size());

				int i = 0;
				assertEquals(List.of("Ägyptologie und Koptologie (Promotion)", "Lastname1", "Firstname1", "1", "(2)", "1,5", "k.A.", "2,5"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("n/a", "Lastname2", "Firstname2", "1", "(2)", "1,5", "k.A.", "2,5"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("n/a", "Lastname3", "Firstname3", "n.b.", "k.A.", "n.b.", "k.A.", "0"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("n/a", "Lastname4", "Firstname4", "n.b.", "k.A.", "n.b.", "k.A.", "0"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("", "Lastname5", "Firstname5", "k.A.", "1", "k.A.", "k.A.", "1"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("n/a", "Lastname6", "Firstname6", "1,5", "(2)", "(0)", "k.A.", "1,5"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("n/a", "Lastname7", "Firstname7", "n.b.", "(2)", "0", "k.A.", "0"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("n/a", "Lastname8", "Firstname8", "k.A.", "(2)", "k.A.", "k.A.", "0"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
			}

			@Test
			void fullListCSV() throws Exception {
				final TextPage downloadFilePage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowLecture?lecture=1&show=csv");
				assertEquals("Studiengang;Nachname;Vorname;eMail;Dynamic Task binary numbers (Pkts: 1);MC Task (Pkts: 2);Hello World (Pkts: 1,5);Something (Pkts: 5);Gesamt\nÄgyptologie und Koptologie (Promotion);Lastname1;Firstname1;user1;1;(2);1,5;k.A.;2,5\n\"n/a;\";Lastname2;Firstname2;user2;1;(2);1,5;k.A.;2,5\n\"n/a;\";Lastname3;Firstname3;user3;n.b.;k.A.;n.b.;k.A.;0\n\"n/a;\";Lastname4;Firstname4;user4;n.b.;k.A.;n.b.;k.A.;0\n;Lastname5;Firstname5;user5;k.A.;1;k.A.;k.A.;1\n\"n/a;\";Lastname6;Firstname6;user6;1,5;(2);(0);k.A.;1,5\n\"n/a;\";Lastname7;Firstname7;user7;n.b.;(2);0;k.A.;0\n\"n/a;\";Lastname8;Firstname8;user8;k.A.;(2);k.A.;k.A.;0\n", downloadFilePage.getContent());
			}
		}

		@Nested
		class PrintableTaskLists {
			@Test
			void noGroup() throws Exception {
				final HtmlPage page = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=3&action=grouplist");

				final HtmlTable table = page.getFirstByXPath("//table");
				final List<HtmlTableRow> rows = table.getRows();
				assertEquals(3, rows.size());

				int i = 0;
				assertEquals(List.of("Abgabe von", "Bemerkungen", "Punkte", "OK?"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("Lastname6, Firstname6", "ne, so nicht", "0", ""), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("Lastname7, Firstname7", "falsch und abnahme nicht bestanden", "0", "ok"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
			}

			@Test
			void group1() throws Exception {
				final HtmlPage page = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=3&action=grouplist&groupid=1");

				final HtmlTable table = page.getFirstByXPath("//table");
				final List<HtmlTableRow> rows = table.getRows();
				assertEquals(2, rows.size());

				int i = 0;
				assertEquals(List.of("Abgabe von", "Bemerkungen", "Punkte", "OK?"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("Lastname1, Firstname1; Lastname2, Firstname2", "jetzt final", "1,5", "ok"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
			}

			@Test
			void group2() throws Exception {
				final HtmlPage page = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/ShowTask?taskid=3&action=grouplist&groupid=2");

				final HtmlTable table = page.getFirstByXPath("//table");
				final List<HtmlTableRow> rows = table.getRows();
				assertEquals(2, rows.size());

				int i = 0;
				assertEquals(List.of("Abgabe von", "Bemerkungen", "Punkte", "OK?"), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
				assertEquals(List.of("Lastname3, Firstname3; Lastname4, Firstname4", "", "n/a", ""), rows.get(i++).getCells().stream().map(c -> c.asNormalizedText()).collect(Collectors.toList()));
			}
		}

		@Nested
		class SearchSubmissions {
			private HtmlForm searchForm;
			private HtmlTextInput searchString;
			private HtmlSubmitInput submitButton;

			@BeforeEach
			void loadSearchPage() throws Exception {
				final HtmlPage searchPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SearchSubmissions?taskid=3");
				assertEquals("GATE: Abgaben durchsuchen", searchPage.getTitleText());
				searchForm = searchPage.getForms().get(0);
				searchString = searchForm.getInputByName("q");
				submitButton = searchForm.getOneHtmlElementByAttribute("input", "type", "submit");
			}

			@Test
			void searchFilesPrintln() throws Exception {
				searchString.type("println");

				final HtmlPage resultsPage = submitButton.click();
				assertEquals("GATE: Suchergebnisse", resultsPage.getTitleText());
				final HtmlTable table = resultsPage.getFirstByXPath("//table");
				assertEquals(3, table.getRows().size());
			}

			@Test
			void searchFilesGaussscheSummenFormel() throws Exception {
				searchString.type("GaussscheSummenFormel");

				final HtmlPage resultsPage = submitButton.click();
				assertEquals("GATE: Suchergebnisse", resultsPage.getTitleText());
				final HtmlTable table = resultsPage.getFirstByXPath("//table");
				assertEquals(1, table.getRows().size());
			}

			@Test
			void searchNothingFound() throws Exception {
				searchString.type("notincluded");

				final HtmlPage resultsPage = submitButton.click();
				assertEquals("GATE: Suchergebnisse", resultsPage.getTitleText());
				final HtmlTable table = resultsPage.getFirstByXPath("//table");
				assertNull(table);
			}
		}
	}

	@Nested
	class LoginAsAdmin {
		@BeforeEach
		void login() throws Exception {
			final HtmlPage loginPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/Overview");
			assertEquals("GATE: Login erforderlich", loginPage.getTitleText());
			final HtmlForm form = loginPage.getFormByName("login");
			final HtmlTextInput username = form.getInputByName("username");
			final HtmlPasswordInput password = form.getInputByName("password");
			username.type("admin");
			password.type("something");
			final HtmlPage overviewPage = form.getOneHtmlElementByAttribute("input", "type", "submit").click();
			assertEquals("GATE: Meine Veranstaltungen", overviewPage.getTitleText());
		}

		@Test
		void adminMenu() throws Exception {
			final HtmlPage adminMenuPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/AdminMenue");
			assertEquals("GATE: Admin-Menü", adminMenuPage.getTitleText());
		}

		@Test
		void selftest() throws Exception {
			final HtmlPage selfTestPage = webClient.getPage(WEBROOT + "/SubmissionInterface/servlets/SelfTest");
			assertEquals("GATE: Selbsttest", selfTestPage.getTitleText());

			final HtmlTable table = selfTestPage.getFirstByXPath("//table");
			for (final HtmlTableRow row : table.getRows()) {
				if ("nein".equals(row.getCell(1).asNormalizedText())) {
					final String content = row.getCell(0).asNormalizedText();
					if (content.startsWith("AuthenticationFilter ist nicht FakeVerify")) {
						continue;
					}
					if (content.startsWith("Servername ist in web.xml gesetzt und sieht gültig aus")) {
						continue;
					}
					if (content.startsWith("Admin-E-Mail-Adresse ist in web.xml gesetzt.")) {
						continue;
					}
					if (content.startsWith("Absender-Adresse ist in web.xml gesetzt.")) {
						continue;
					}
					if (content.startsWith("Test-Mail wurde an Admin-E-Mail-Adresse")) {
						continue;
					}
					if (content.startsWith("Parent vom Daten-Verzeichnis ist nicht beschreibbar")) {
						continue;
					}
					fail(row.getCell(0).asNormalizedText());
				}
			}
		}
	}
}
