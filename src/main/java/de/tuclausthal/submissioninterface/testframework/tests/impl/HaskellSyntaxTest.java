/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Esat Avci <e.avci@campus.lmu.de>
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

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

public class HaskellSyntaxTest extends TempDirTest<de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest> {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String SAFE_DOCKER_SCRIPT = "/usr/local/bin/safe-docker";
    private static final Random RANDOM = new Random();
    private final String separator;
    private Path tempDir;

    public HaskellSyntaxTest(de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest test) {
        super(test);
        this.separator = "#<GATE@" + RANDOM.nextLong() + "#@>#";
    }

    @Override
    public void performTest(Path basePath, Path submissionPath, TestExecutorTestResult testResult) throws Exception {
        try {
            tempDir = Util.createTemporaryDirectory("test");
            if (tempDir == null) {
                throw new IOException("Failed to create tempdir!");
            }

            Path adminDir = tempDir.resolve("admin");
            Path studentDir = tempDir.resolve("student");
            Files.createDirectories(adminDir);
            Files.createDirectories(studentDir);

            Util.recursiveCopy(submissionPath, studentDir);

            String bashScript = """
                #!/bin/bash
                set -e
                echo '%s'
                for file in *.hs; do
                  ghci -ignore-dot-ghci -v0 -ferror-spans -fdiagnostics-color=never -Wall -e ":load $file" -e ":quit"
                done
                """.formatted(separator);

            Path testScript = adminDir.resolve("test.sh");
            try (Writer writer = Files.newBufferedWriter(testScript)) {
                writer.write(bashScript);
            }

            List<String> cmd = List.of(
                    "sudo",
                    SAFE_DOCKER_SCRIPT,
                    "--timeout=" + test.getTimeout(),
                    "--dir=" + Util.escapeCommandlineArguments(adminDir.toAbsolutePath().toString()),
                    "--",
                    "bash",
                    Util.escapeCommandlineArguments(testScript.toAbsolutePath().toString())
            );

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(studentDir.toFile());
            pb.environment().keySet().removeIf(k -> !List.of("PATH", "USER", "LANG").contains(k));

            LOG.debug("Executing HaskellSyntaxTest docker process: {}", cmd);
            Process proc = pb.start();
            ProcessOutputGrabber outputGrabber = new ProcessOutputGrabber(proc);

            int exitCode = -1;
            boolean aborted = false;
            try {
                exitCode = proc.waitFor();
            } catch (InterruptedException e) {
                aborted = true;
            }
            outputGrabber.waitFor();

            if (exitCode == 23 || exitCode == 24) aborted = true;

            boolean success = (exitCode == 0) && !outputGrabber.getStdErrBuffer().toString().toLowerCase().contains("error:");

            testResult.setTestPassed(success);
            testResult.setTestOutput(generateJsonResult(outputGrabber.getStdOutBuffer(), outputGrabber.getStdErrBuffer(), exitCode, success, aborted));

        } finally {
            if (tempDir != null) {
                Util.recursiveDelete(tempDir);
            }
        }
    }

    private String generateJsonResult(StringBuffer stdout, StringBuffer stderr, int exitCode, boolean success, boolean aborted) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("stdout", stdout.toString())
                .add("separator", separator + "\n")
                .add("exitCode", exitCode)
                .add("exitedCleanly", success);

        if (stderr.length() > 0) {
            builder.add("stderr", stderr.toString());
        }
        if (aborted) {
            builder.add("time-exceeded", true);
        }
        if (tempDir != null) {
            builder.add("tmpdir", tempDir.toAbsolutePath().toString());
        }
        return builder.build().toString();
    }

    @Override
    protected void performTestInTempDir(Path basePath, Path tempDir, TestExecutorTestResult testResult) throws Exception {
        //currently unused
    }
}
