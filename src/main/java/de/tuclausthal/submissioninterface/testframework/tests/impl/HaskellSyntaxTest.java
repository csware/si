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

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;


public class HaskellSyntaxTest extends DockerTest {

    public HaskellSyntaxTest(de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest test) {
        super(test);
    }
    @Override
    protected String generateTestShellScript() {
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
    protected boolean isSuccessful(int exitCode, StringBuffer stderr) {
        return exitCode == 0 && !stderr.toString().toLowerCase().contains("error:");
    }

    @Override
    protected String generateJsonResult(StringBuffer stdout, StringBuffer stderr, int exitCode, boolean success, boolean aborted) {
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
}
