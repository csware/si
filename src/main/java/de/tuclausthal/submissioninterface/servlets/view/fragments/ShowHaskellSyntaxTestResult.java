package de.tuclausthal.submissioninterface.servlets.view.fragments;

import java.io.PrintWriter;
import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest;

public class ShowHaskellSyntaxTestResult {
    /**
     * @param out         PrintWriter zum Ausgeben
     * @param test        der HaskellSyntaxTest
     * @param testOutput  JSON-String aus testResult.getTestOutput()
     * @param isStudent   true, wenn Studentensicht, false wenn Tutorsicht
     * @param javaScript  Hier kannst Du Code-Preview/JS-Schnipsel anfügen, wenn Du willst
     */
    public static void printTestResults(PrintWriter out, HaskellSyntaxTest test, String testOutput, boolean isStudent, StringBuilder javaScript) {
        // JSON auslesen:
        JsonObject json = Json.createReader(new StringReader(testOutput)).readObject();

        String stderr = json.getString("stderr", "");

        // ErrorMessage
        if (!stderr.isEmpty()) {
            out.println("<p><strong>Fehlerausgabe (stderr):</strong></p>");
            // Du kannst Zeilen hier farblich kennzeichnen oder ab Zeilenumbrüchen splitten
            out.println("<pre class=\"haskellstderr\">" + escapeHTML(stderr) + "</pre>");
        }

        out.println("</div>");
    }

    private static String escapeHTML(String str) {
        if (str == null) {
            return "";
        }
        return str
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}