package de.tuclausthal.submissioninterface.persistence.datamodel;

import java.lang.invoke.MethodHandles;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

@Entity
public class HaskellSyntaxTest extends Test {
    private static final long serialVersionUID = 1L;

    @Override
    @Transient
    @JsonIgnore
    public AbstractTest<HaskellSyntaxTest> getTestImpl() {
        return new de.tuclausthal.submissioninterface.testframework.tests.impl.HaskellSyntaxTest(this);
    }

    @Override
    @Transient
    public boolean TutorsCanRun() {
        return true;
    }

    @Override
    public String toString() {
        return MethodHandles.lookup().lookupClass().getSimpleName()
                + " (" + Integer.toHexString(hashCode()) + "): id:" + getId()
                + "; testtitle:" + getTestTitle();
    }
}
