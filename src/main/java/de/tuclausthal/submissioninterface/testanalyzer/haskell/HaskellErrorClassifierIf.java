package de.tuclausthal.submissioninterface.testanalyzer.haskell;

import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;

public interface HaskellErrorClassifierIf {
    void classify(TestResult testResult, String stderr, String keyStr);
}