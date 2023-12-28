/*
 * Copyright 2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTestStep;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

public class JavaAdvancedIOTestTest {

	@TempDir
	private File tempDir;
	private File sourceDir;
	private File dataDir;
	private JavaAdvancedIOTest javaAdvancedIOTest;
	private de.tuclausthal.submissioninterface.testframework.tests.impl.JavaAdvancedIOTest javaFunctionTest;

	@BeforeEach
	void setUpTest() throws IOException {
		sourceDir = new File(tempDir, "source");
		sourceDir.mkdir();
		dataDir = new File(tempDir, "data");
		dataDir.mkdir();
		FileUtils.copyFileToDirectory(new File("SecurityManager/NoExitSecurityManager.jar"), dataDir);

		javaAdvancedIOTest = new JavaAdvancedIOTest();
		javaFunctionTest = new de.tuclausthal.submissioninterface.testframework.tests.impl.JavaAdvancedIOTest(javaAdvancedIOTest);
	}

	@Test
	void testJavaAdvancedIOTestOK() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Triangle.java"))) {
			fw.write("public class Triangle {\n" + "	static void drawTriangle(int sizeOfTriangle) {\n" + "		for (int i = 0; i < sizeOfTriangle; i++) {\n" + "			for (int j = 0; j <= i; j++) {\n" + "				System.out.print(\"*\");\n" + "			}\n" + "			System.out.println();\n" + "		}\n" + "	}\n" + "}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "One star", "Triangle.drawTriangle(1);", "*"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Two stars", "Triangle.drawTriangle(2);", "*\n**"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Four stars", "Triangle.drawTriangle(4);", "*\n**\n***\n****"));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertTrue(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"#<GATE@6881399865488942908#@>#\n*\n#<GATE@6881399865488942908#@>#\n*\n**\n#<GATE@6881399865488942908#@>#\n*\n**\n***\n****\n","stderr":"#<GATE@6881399865488942908#@>#\n#<GATE@6881399865488942908#@>#\n#<GATE@6881399865488942908#@>#\n","separator":"#<GATE@6881399865488942908#@>#\n","exitedCleanly":true,"steps":[{"id":0,"got":"","expected":"","ok":true},{"id":0,"got":"*\n","expected":"*","ok":true},{"id":0,"got":"*\n**\n","expected":"*\n**","ok":true},{"id":0,"got":"*\n**\n***\n****\n","expected":"*\n**\n***\n****","ok":true}]}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("separator"));
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertTrue(object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly"));
		assertFalse(object.containsKey("missing-tests"));
		assertFalse(object.containsKey("time-exceeded"));
		assertFalse(object.containsKey("syntaxerror"));
		assertTrue(object.containsKey("steps") && object.get("steps").getValueType().equals(JsonValue.ValueType.ARRAY));

		JsonArray array = object.get("steps").asJsonArray();
		assertEquals(4, array.size());
		for (int i = 0; i < array.size(); ++i) {
			JsonObject stepObject = array.get(i).asJsonObject();
			assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok"));
		}
	}

	@Test
	void testJavaAdvancedIOTestFailedStep() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Triangle.java"))) {
			fw.write("public class Triangle {\n" + "	static void drawTriangle(int sizeOfTriangle) {\n" + "		for (int i = 0; i < sizeOfTriangle; i++) {\n" + "			for (int j = 0; j <= i; j++) {\n" + "				System.out.print(\"*\");\n" + "			}\n" + "			System.out.println();\n" + "		}\n" + "	}\n" + "}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "One star", "Triangle.drawTriangle(1);", "*"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Two stars", "Triangle.drawTriangle(2);", "*\n**"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Four stars", "Triangle.drawTriangle(4);", "something else"));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"#<GATE@-1025328391251392430#@>#\n*\n#<GATE@-1025328391251392430#@>#\n*\n**\n#<GATE@-1025328391251392430#@>#\n*\n**\n***\n****\n","stderr":"#<GATE@-1025328391251392430#@>#\n#<GATE@-1025328391251392430#@>#\n#<GATE@-1025328391251392430#@>#\n","separator":"#<GATE@-1025328391251392430#@>#\n","exitedCleanly":true,"steps":[{"id":0,"got":"","expected":"","ok":true},{"id":0,"got":"*\n","expected":"*","ok":true},{"id":0,"got":"*\n**\n","expected":"*\n**","ok":true},{"id":0,"got":"*\n**\n***\n****\n","expected":"something else","ok":false}]}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("separator"));
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertTrue(object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly"));
		assertFalse(object.containsKey("missing-tests"));
		assertFalse(object.containsKey("time-exceeded"));
		assertFalse(object.containsKey("syntaxerror"));
		assertTrue(object.containsKey("steps") && object.get("steps").getValueType().equals(JsonValue.ValueType.ARRAY));

		JsonArray array = object.get("steps").asJsonArray();
		assertEquals(4, array.size());
		for (int i = 0; i < array.size() - 1; ++i) {
			JsonObject stepObject = array.get(i).asJsonObject();
			assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok"));
		}
		JsonObject stepObject = array.get(array.size() - 1).asJsonObject();
		assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok") == false);
	}

	@Test
	void testJavaAdvancedIOTestException() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Triangle.java"))) {
			fw.write("public class Triangle {\n" + "	static void drawTriangle(int sizeOfTriangle) {\n" + "		if (sizeOfTriangle == 2) { throw new RuntimeException(\"Some failure!\"); }\n		for (int i = 0; i < sizeOfTriangle; i++) {\n" + "			for (int j = 0; j <= i; j++) {\n" + "				System.out.print(\"*\");\n" + "			}\n" + "			System.out.println();\n" + "		}\n" + "	}\n" + "}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "One star", "Triangle.drawTriangle(1);", "*"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Two stars", "Triangle.drawTriangle(2);", "*\n**"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Four stars", "Triangle.drawTriangle(4);", "*\n**\n***\n****"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Four stars", "Triangle.drawTriangle(4);", "something else"));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"#<GATE@-5109496287853452609#@>#\n*\n#<GATE@-5109496287853452609#@>#\n","stderr":"#<GATE@-5109496287853452609#@>#\n#<GATE@-5109496287853452609#@>#\nException in thread \"main\" java.lang.RuntimeException: Some failure!\n\tat Triangle.drawTriangle(Triangle.java:3)\n\tat Tester.main(Tester.java:8)\n","separator":"#<GATE@-5109496287853452609#@>#\n","exitedCleanly":false,"steps":[{"id":0,"got":"","expected":"","ok":true},{"id":0,"got":"*\n","expected":"*","ok":true},{"id":0,"got":"","expected":"*\n**","ok":false}],"missing-tests":true}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("separator"));
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertTrue(object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly") == false);
		assertTrue(object.containsKey("missing-tests"));
		assertTrue(object.getBoolean("missing-tests"));
		assertFalse(object.containsKey("time-exceeded"));
		assertFalse(object.containsKey("syntaxerror"));
		assertTrue(object.containsKey("steps") && object.get("steps").getValueType().equals(JsonValue.ValueType.ARRAY));

		JsonArray array = object.get("steps").asJsonArray();
		assertEquals(3, array.size());
		for (int i = 0; i < array.size() - 1; ++i) {
			JsonObject stepObject = array.get(i).asJsonObject();
			assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok"));
		}
		JsonObject stepObject = array.get(array.size() - 1).asJsonObject();
		assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok") == false);
		assertTrue(object.getString("stderr").contains(" java.lang.RuntimeException: Some failure!"), "Found java.lang.RuntimeException: Some failure! in stderr");
	}

	@Test
	void testJavaAdvancedIOTestSyntaxErrorInStudentCode() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Triangle.java"))) {
			fw.write("public class Triangle {\n" + "	static void drawTriangle(it sizeOfTriangle) {\n" + "		for (int i = 0; i < sizeOfTriangle; i++) {\n" + "			for (int j = 0; j <= i; j++) {\n" + "				System.out.print(\"*\");\n" + "			}\n" + "			System.out.println();\n" + "		}\n" + "	}\n" + "}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"","stderr":"Triangle.java:2: error: cannot find symbol\r\n\tstatic void drawTriangle(it sizeOfTriangle) {\r\n\t                         ^\r\n  symbol:   class it\r\n  location: class Triangle\r\n1 error\r\n","syntaxerror":"student-code"}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertEquals("", object.getString("stdout"));
		assertTrue(object.containsKey("syntaxerror"));
		assertEquals("student-code", object.getString("syntaxerror"));
		assertTrue(object.getString("stderr").contains("Triangle.java:2: error: cannot find symbol"), object.getString("stderr"));
	}

	@Test
	void testJavaAdvancedIOTestMissingMethodInStudentCode() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Triangle.java"))) {
			fw.write("public class Triangle {\n" + "	static void drawTheTriangle(int sizeOfTriangle) {\n" + "		for (int i = 0; i < sizeOfTriangle; i++) {\n" + "			for (int j = 0; j <= i; j++) {\n" + "				System.out.print(\"*\");\n" + "			}\n" + "			System.out.println();\n" + "		}\n" + "	}\n" + "}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"","stderr":"Tester.java:4: error: cannot find symbol\r\nTriangle.drawTriangle(0);}\r\n        ^\r\n  symbol:   method drawTriangle(int)\r\n  location: class Triangle\r\n1 error\r\n","syntaxerror":"with-test-code"}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertEquals("", object.getString("stdout"));
		assertTrue(object.containsKey("syntaxerror"));
		assertEquals("with-test-code", object.getString("syntaxerror"));
		assertTrue(object.getString("stderr").replace("\r\n", "\n").contains("error: cannot find symbol\n" + "Triangle.drawTriangle(0);}"), object.getString("stderr"));
	}

	@Test
	void testJavaAdvancedIOTestSetIOInTestCode() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "HelloWorld.java"))) {
			fw.write("public class HelloWorld {\n	static void main(String[] args) {\n		System.out.print(\"Hello World!\");\n" + "	}\n}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Hello World! (ignore case)", "var old = System.out;\nvar buffer = new java.io.ByteArrayOutputStream();\nSystem.setOut(new java.io.PrintStream(buffer));\n\nHelloWorld.main(null);\n\nSystem.setOut(old);\nString content = buffer.toString();\nSystem.out.println(content.toLowerCase());", "hello world!"));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertTrue(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"hello world!\n","separator":"#<GATE@-5095029310556099959#@>#\n","exitedCleanly":true,"steps":[{"id":0,"got":"hello world!\n","expected":"hello world!","ok":true}]}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("separator"));
		assertFalse(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertTrue(object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly"));
		assertFalse(object.containsKey("missing-tests"));
		assertFalse(object.containsKey("time-exceeded"));
		assertFalse(object.containsKey("syntaxerror"));
		assertTrue(object.containsKey("steps") && object.get("steps").getValueType().equals(JsonValue.ValueType.ARRAY));

		JsonArray array = object.get("steps").asJsonArray();
		assertEquals(1, array.size());
		for (int i = 0; i < array.size(); ++i) {
			JsonObject stepObject = array.get(i).asJsonObject();
			assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok"));
		}
	}

	@Test
	void testJavaAdvancedIOTestSystemExitInStudentCode() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Evil.java"))) {
			fw.write("public class Evil {\n	static void main(String[] args) {\n		System.exit(0);\n" + "	}\n}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Hello World! (ignore case)", "Evil.main(null);", ""));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"","stderr":"Exception in thread \"main\" java.security.AccessControlException: access denied (\"java.lang.RuntimePermission\" \"exitTheVM.0\")\n\tat java.base/java.security.AccessControlContext.checkPermission(AccessControlContext.java:485)\n\tat java.base/java.security.AccessController.checkPermission(AccessController.java:1068)\n\tat java.base/java.lang.SecurityManager.checkPermission(SecurityManager.java:416)\n\tat secmgr.NoExitSecurityManager.checkExit(NoExitSecurityManager.java:33)\n\tat java.base/java.lang.Runtime.exit(Runtime.java:113)\n\tat java.base/java.lang.System.exit(System.java:1860)\n\tat Evil.main(Evil.java:3)\n\tat Tester.main(Tester.java:4)\n","separator":"#<GATE@-7844624952352875734#@>#\n","exitedCleanly":false,"steps":[{"id":0,"got":"","expected":"","ok":true}]}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("separator"));
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertTrue(object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly") == false);
		assertFalse(object.containsKey("missing-tests"));
		assertFalse(object.containsKey("time-exceeded"));
		assertFalse(object.containsKey("syntaxerror"));
		assertTrue(object.containsKey("steps") && object.get("steps").getValueType().equals(JsonValue.ValueType.ARRAY));

		JsonArray array = object.get("steps").asJsonArray();
		assertEquals(1, array.size());
		JsonObject stepObject = array.get(array.size() - 1).asJsonObject();
		assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok") == true);
		assertTrue(object.getString("stderr").contains(" java.security.AccessControlException: access denied (\"java.lang.RuntimePermission\" \"exitTheVM."), "Found java.security.AccessControlException: access denied (\"java.lang.RuntimePermission\" \"exitTheVM. in stderr");
	}

	@Test
	void testJavaAdvancedIOTestSetIOInStudentCode() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Evil.java"))) {
			fw.write("public class Evil {\n	static void main(String[] args) {\n		System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));\n" + "	}\n}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Hello World! (ignore case)", "Evil.main(null);", "something"));

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"","stderr":"Exception in thread \"main\" java.security.AccessControlException: access denied (\"java.lang.RuntimePermission\" \"setIO\")\n\tat java.base/java.security.AccessControlContext.checkPermission(AccessControlContext.java:485)\n\tat java.base/java.security.AccessController.checkPermission(AccessController.java:1068)\n\tat java.base/java.lang.SecurityManager.checkPermission(SecurityManager.java:416)\n\tat java.base/java.lang.System.checkIO(System.java:323)\n\tat java.base/java.lang.System.setOut(System.java:240)\n\tat Evil.main(Evil.java:3)\n\tat Tester.main(Tester.java:4)\n","separator":"#<GATE@571104252856889185#@>#\n","exitedCleanly":false,"steps":[{"id":0,"got":"","expected":"","ok":true}]}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("separator"));
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertTrue(object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly") == false);
		assertFalse(object.containsKey("missing-tests"));
		assertFalse(object.containsKey("time-exceeded"));
		assertFalse(object.containsKey("syntaxerror"));
		assertTrue(object.containsKey("steps") && object.get("steps").getValueType().equals(JsonValue.ValueType.ARRAY));

		JsonArray array = object.get("steps").asJsonArray();
		assertEquals(1, array.size());
		JsonObject stepObject = array.get(array.size() - 1).asJsonObject();
		assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok") == false);
		assertTrue(object.getString("stderr").contains(" java.security.AccessControlException: access denied (\"java.lang.RuntimePermission\" \"setIO\")"), "Found java.security.AccessControlException: access denied (\"java.lang.RuntimePermission\" \"setIO\") in stderr");
	}

	@Test
	void testJavaAdvancedIOTestTimeout() throws Exception {
		try (Writer fw = new FileWriter(new File(sourceDir, "Triangle.java"))) {
			fw.write("public class Triangle {\n" + "	static void drawTriangle(int sizeOfTriangle) {\n" + "		if (sizeOfTriangle == 2) { try { Thread.sleep(10000); } catch (InterruptedException e) {} }\n		for (int i = 0; i < sizeOfTriangle; i++) {\n" + "			for (int j = 0; j <= i; j++) {\n" + "				System.out.print(\"*\");\n" + "			}\n" + "			System.out.println();\n" + "		}\n" + "	}\n" + "}");
		}

		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "One star", "Triangle.drawTriangle(1);", "*"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Two stars", "Triangle.drawTriangle(2);", "*\n**"));
		javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Four stars", "Triangle.drawTriangle(4);", "something else"));
		javaAdvancedIOTest.setTimeout(2);

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());

		// {"v":1,"stdout":"#<GATE@5822909744084546135#@>#\n*\n#<GATE@5822909744084546135#@>#\n","stderr":"#<GATE@5822909744084546135#@>#\n#<GATE@5822909744084546135#@>#\n","separator":"#<GATE@5822909744084546135#@>#\n","exitedCleanly":false,"time-exceeded":true,"steps":[{"id":0,"got":"","expected":"","ok":true},{"id":0,"got":"*\n","expected":"*","ok":true},{"id":0,"got":"","expected":"*\n**","ok":false}],"missing-tests":true}
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		assertNotNull(object);
		assertTrue(object.containsKey("separator"));
		assertTrue(object.containsKey("stderr"));
		assertTrue(object.containsKey("stdout"));
		assertTrue(object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly") == false);
		assertTrue(object.containsKey("missing-tests"));
		assertTrue(object.getBoolean("missing-tests"));
		assertTrue(object.containsKey("time-exceeded"));
		assertTrue(object.getBoolean("time-exceeded"));
		assertFalse(object.containsKey("syntaxerror"));
		assertTrue(object.containsKey("steps") && object.get("steps").getValueType().equals(JsonValue.ValueType.ARRAY));

		JsonArray array = object.get("steps").asJsonArray();
		assertEquals(3, array.size());
		for (int i = 0; i < array.size() - 1; ++i) {
			JsonObject stepObject = array.get(i).asJsonObject();
			assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok"));
		}
		JsonObject stepObject = array.get(array.size() - 1).asJsonObject();
		assertTrue(stepObject.containsKey("ok") && stepObject.getBoolean("ok") == false);
	}
}
