/**
 * 
 */
package org.ubimix.commons.parser.css;

import java.util.List;

import junit.framework.TestCase;

import org.ubimix.commons.parser.CharStream;
import org.ubimix.commons.parser.ICharStream;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssAttrSelectorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssCompositeAttrSelectorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssSimpleAttrSelectorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssTagSelectorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssTagSelectorTokenizer;

/**
 * @author kotelnikov
 */
public class CssSelectorTokenizerTest extends TestCase {

    /**
     * @param name
     */
    public CssSelectorTokenizerTest(String name) {
        super(name);
    }

    private String[] match(String name, String type, String value) {
        return new String[] { name, type, value };
    }

    // public void testAttributeSelectorTokenizer() throws Exception {
    public void test() throws Exception {
        testAttributeSelectorTokenizer(
            "a[ x = y ]#123",
            "a",
            "[ x = y ]",
            "#123");

        testAttributeSelectorTokenizer("*", "*");
        testAttributeSelectorTokenizer("a", "a");
        testAttributeSelectorTokenizer("a|b", "a|b");
        testAttributeSelectorTokenizer("*", "*");
        testAttributeSelectorTokenizer("*|b", "*|b");
        testAttributeSelectorTokenizer("a|*", "a|*");
        testAttributeSelectorTokenizer("a.x", "a", ".x");
        testAttributeSelectorTokenizer("a.x.y.z", "a", ".x", ".y", ".z");
        testAttributeSelectorTokenizer("a.x.y#z", "a", ".x", ".y", "#z");
        testAttributeSelectorTokenizer("a[foo bar]", "a", "[foo bar]");
        testAttributeSelectorTokenizer(
            "x.a[b=c].d#e",
            "x",
            ".a",
            "[b=c]",
            ".d",
            "#e");
        testAttributeSelectorTokenizer(
            "a.x#y[foo bar]",
            "a",
            ".x",
            "#y",
            "[foo bar]");
        testAttributeSelectorTokenizer(
            "a.x#y[foo bar][boo woo]",
            "a",
            ".x",
            "#y",
            "[foo bar]",
            "[boo woo]");
        testAttributeSelectorTokenizer(
            "a[foo bar].x[boo woo]#y",
            "a",
            "[foo bar]",
            ".x",
            "[boo woo]",
            "#y");

        testAttributeSelectorTokenizer(".x", null, ".x");
        testAttributeSelectorTokenizer(".x.y.z", null, ".x", ".y", ".z");
        testAttributeSelectorTokenizer(".x.y#z", null, ".x", ".y", "#z");
        testAttributeSelectorTokenizer("[foo bar]", null, "[foo bar]");
        testAttributeSelectorTokenizer(
            ".titi[ a = b]#abc",
            null,
            ".titi",
            "[ a = b]",
            "#abc");
        testAttributeSelectorTokenizer(
            ".titi[ a = b ]#abc",
            null,
            ".titi",
            "[ a = b ]",
            "#abc");

        testAttributeSelectorTokenizer(
            ".x#y[foo bar]",
            null,
            ".x",
            "#y",
            "[foo bar]");
        testAttributeSelectorTokenizer(
            ".x#y[foo bar][boo woo]",
            null,
            ".x",
            "#y",
            "[foo bar]",
            "[boo woo]");
        testAttributeSelectorTokenizer(
            "[foo bar].x[boo woo]#y",
            null,
            "[foo bar]",
            ".x",
            "[boo woo]",
            "#y");
        testAttributeSelectorTokenizer(
            "[ foo ~= 'Hello, world' bar |= ].x[boo woo]#y",
            null,
            "[ foo ~= 'Hello, world' bar |= ]",
            ".x",
            "[boo woo]",
            "#y");
        testAttributeSelectorTokenizer(
            "[ foo ~= 'Hello, world' bar |= xxx].x[boo woo]#y",
            null,
            "[ foo ~= 'Hello, world' bar |= xxx]",
            ".x",
            "[boo woo]",
            "#y");
    }

    public void testAttributeMatches() {
        testAttributeMatches("a[ prop1 ] ", "a", match("prop1", "", ""));
        testAttributeMatches(
            "ns|a[ ns|prop1 ] ",
            "ns|a",
            match("ns|prop1", "", ""));

        testAttributeMatches("a[ prop1 =] ", "a", match("prop1", "=", ""));
        testAttributeMatches(
            "a[ prop1 =value] ",
            "a",
            match("prop1", "=", "value"));
        testAttributeMatches(
            "a[ prop1 = ' value '] ",
            "a",
            match("prop1", "=", "' value '"));
        testAttributeMatches(
            "a[ prop1 prop2 ] ",
            "a",
            match("prop1", "", ""),
            match("prop2", "", ""));
        testAttributeMatches(
            "a[ prop1 prop2 ~= ] ",
            "a",
            match("prop1", "", ""),
            match("prop2", "~=", ""));
        testAttributeMatches(
            "a[ prop1 prop2 ~= ] ",
            "a",
            match("prop1", "", ""),
            match("prop2", "~=", ""));
        testAttributeMatches(
            "a[ prop1 ~= 'value1'] ",
            "a",
            match("prop1", "~=", "'value1'"));
        testAttributeMatches(
            "a[ prop1 = ' value '  !] ",
            "a",
            match("prop1", "=", "' value '"));

        testAttributeMatches(
            "a[ * prop1 = ' value '  !] ",
            "a",
            match("*", "", ""),
            match("prop1", "=", "' value '"));

        // Broken selectors
        testAttributeMatches(
            "a[ prop1 = ' value '  !] ",
            "a",
            match("prop1", "=", "' value '"));
        testAttributeMatches(
            "a[ .. prop1 = ' value '  !  prop2 ~= value2 ] ",
            "a",
            match("prop1", "=", "' value '"),
            match("prop2", "~=", "value2"));

    }

    private void testAttributeMatches(
        String str,
        String name,
        String[]... matches) {
        ICharStream stream = new CharStream(str);
        CssTagSelectorToken token = new CssTagSelectorTokenizer().read(stream);
        assertNotNull(token);
        String nameToken = token.getNameToken();
        if (name != null) {
            assertNotNull(nameToken);
            assertEquals(name, nameToken);
        } else {
            assertNull(nameToken);
        }
        List<CssAttrSelectorToken> attributeTokens = token
            .getAttributSelectors();
        assertEquals(1, attributeTokens.size());
        CssAttrSelectorToken attrToken = attributeTokens.get(0);
        assertTrue(attrToken instanceof CssCompositeAttrSelectorToken);
        CssCompositeAttrSelectorToken t = (CssCompositeAttrSelectorToken) attrToken;

        List<CssSimpleAttrSelectorToken> matchTokens = t
            .getAttrSelectorTokens();
        assertEquals(matches.length, matchTokens.size());
        int i = 0;
        for (String[] match : matches) {
            String matchName = match[0];
            String matchType = match[1];
            String matchValue = match[2];
            CssSimpleAttrSelectorToken matchToken = matchTokens.get(i++);
            assertEquals(matchName, matchToken.getAttrName());
            assertEquals(matchType, matchToken.getMatchType() + "");
            assertEquals(matchValue, matchToken.getMatchValue());
        }
    }

    private void testAttributeSelectorTokenizer(
        String str,
        String name,
        String... attrs) {
        ICharStream stream = new CharStream(str);
        CssTagSelectorToken token = new CssTagSelectorTokenizer().read(stream);
        assertNotNull(token);
        String nameToken = token.getNameToken();
        if (name != null) {
            assertNotNull(nameToken);
            assertEquals(name, nameToken);
        } else {
            assertNull(nameToken);
        }
        List<CssAttrSelectorToken> attributeTokens = token
            .getAttributSelectors();
        if (attrs.length > 0) {
            assertEquals(attrs.length, attributeTokens.size());
            int i = 0;
            for (String attr : attrs) {
                CssAttrSelectorToken attrToken = attributeTokens.get(i++);
                assertEquals(attr, attrToken.getText());
            }
        } else {
            assertTrue(attributeTokens.isEmpty());
        }
    }

}
