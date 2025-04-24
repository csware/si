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

public class HaskellSyntaxTest extends DockerTest {

    public HaskellSyntaxTest(de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest test) {
        super(test);
    }

    @Override
    protected void analyzeAndSetResult(boolean exitedCleanly, StringBuffer stdout, StringBuffer stderr, int exitCode, boolean aborted, TestExecutorTestResult result) {
        boolean success = exitedCleanly && !stderr.toString().toLowerCase().contains("error:");
        result.setTestPassed(success);
        result.setTestOutput(createJsonBuilder(success, stdout, stderr, exitCode, aborted).build().toString());
    }


    @Override
    protected void performTestInTempDir(Path basePath, Path tempDir, TestExecutorTestResult testResult) throws Exception {
        //currently unused
    }

    @Override
    protected String generateTestShellScript(){
        return """
                #!/bin/bash
                set -e
                echo '%s'
                for file in *.hs; do
                  ghci -ignore-dot-ghci -v0 -ferror-spans -fdiagnostics-color=never -Wall -e ":load $file" -e ":quit"
                done
                """.formatted(separator);
    }

    @Override
    protected void debugLog(List<String> params, Path studentDir){
        LOG.debug("Executing HaskellSyntaxTest docker process: {} in {}", params, studentDir);
    }
}
