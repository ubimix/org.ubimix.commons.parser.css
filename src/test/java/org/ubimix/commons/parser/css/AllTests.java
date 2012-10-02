package org.ubimix.commons.parser.css;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
            "Test for org.ubimix.commons.parser.css");
        // $JUnit-BEGIN$
        suite.addTestSuite(CssSelectorParserTest.class);
        suite.addTestSuite(CssSelectorTokenizerTest.class);
        // $JUnit-END$
        return suite;
    }
}
