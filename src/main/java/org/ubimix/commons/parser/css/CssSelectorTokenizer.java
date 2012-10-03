/**
 * 
 */
package org.ubimix.commons.parser.css;

import java.util.ArrayList;
import java.util.List;

import org.ubimix.commons.parser.AbstractTokenizer;
import org.ubimix.commons.parser.CharStream;
import org.ubimix.commons.parser.CharStream.Marker;
import org.ubimix.commons.parser.CharStream.Pointer;
import org.ubimix.commons.parser.CompositeTokenizer;
import org.ubimix.commons.parser.StreamToken;
import org.ubimix.commons.parser.base.QuotedValueTokenizer;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssSimpleAttrSelectorToken.MatchType;

/**
 * @author kotelnikov
 */
public class CssSelectorTokenizer extends CompositeTokenizer {

    /**
     * @author kotelnikov
     */
    public static class CssAttrSelectorToken extends StreamToken {
    }

    /**
     * @author kotelnikov
     */
    public static class CssCombinatorToken extends StreamToken {

        private char fType;

        public char getType() {
            return fType;
        }

        public void setType(char type) {
            fType = type;
        }

    }

    private static class CssCombinatorTokenizer extends AbstractTokenizer {

        @Override
        protected CssCombinatorToken newToken() {
            return new CssCombinatorToken();
        }

        @Override
        public CssCombinatorToken read(CharStream stream) {
            CssCombinatorToken result = null;
            StringBuilder buf = null;
            char type = 0;
            Pointer begin = stream.getPointer();
            char ch = stream.getChar();
            while (Character.isSpaceChar(ch)) {
                if (buf == null) {
                    buf = new StringBuilder();
                }
                buf.append(ch);
                type = ' ';
                if (!stream.incPos()) {
                    break;
                }
                ch = stream.getChar();
            }
            if (type == 0) {
                switch (ch) {
                    case '+':
                    case '>':
                    case ',':
                    case '~':
                        if (buf == null) {
                            buf = new StringBuilder();
                        }
                        buf.append(ch);
                        type = ch;
                        stream.incPos();
                        break;
                }
            }
            if (type != 0) {
                Pointer end = stream.getPointer();
                result = newToken(begin, end, buf.toString());
                result.setType(type);
            }
            return result;
        }
    }

    public static class CssCompositeAttrSelectorToken
        extends
        CssAttrSelectorToken {
        private List<CssSimpleAttrSelectorToken> fAttrSelectorTokens = new ArrayList<CssSimpleAttrSelectorToken>();

        public List<CssSimpleAttrSelectorToken> getAttrSelectorTokens() {
            return fAttrSelectorTokens;
        }

        public void setAttrSelectorTokens(
            List<CssSimpleAttrSelectorToken> tokens) {
            if (tokens != null) {
                fAttrSelectorTokens.clear();
                fAttrSelectorTokens.addAll(tokens);
            }
        }
    }

    private static class CssCompositeAttrSelectorTokenizer
        extends
        AbstractTokenizer {

        public static CssCompositeAttrSelectorTokenizer INSTANCE = new CssCompositeAttrSelectorTokenizer();

        @Override
        protected CssCompositeAttrSelectorToken newToken() {
            return new CssCompositeAttrSelectorToken();
        }

        @Override
        public CssCompositeAttrSelectorToken read(CharStream stream) {
            char ch = stream.getChar();
            CssCompositeAttrSelectorToken result = null;
            if (ch == '[') {
                Marker marker = stream.markPosition();
                try {
                    stream.incPos();
                    ch = stream.getChar();
                    List<CssSimpleAttrSelectorToken> attrTokens = null;
                    while (ch != ']' && !stream.isTerminated()) {
                        skipSpaces(stream);
                        CssSimpleAttrSelectorToken attrToken = CssInternalAttrSelectorTokenizer.INSTANCE
                            .read(stream);
                        if (attrToken != null) {
                            if (attrTokens == null) {
                                attrTokens = new ArrayList<CssSimpleAttrSelectorToken>();
                            }
                            attrTokens.add(attrToken);
                            ch = stream.getChar();
                        } else {
                            ch = stream.getChar();
                            if (ch == ']') {
                                break;
                            }
                            stream.incPos();
                            ch = stream.getChar();
                        }
                    }
                    stream.incPos();
                    Pointer begin = marker.getPointer();
                    Pointer end = stream.getPointer();
                    String str = marker.getSubstring(begin, end);
                    result = newToken(begin, end, str);
                    result.setAttrSelectorTokens(attrTokens);
                } finally {
                    marker.close(false);
                }
            }
            return result;
        }

    }

    private static class CssInternalAttrSelectorTokenizer
        extends
        AbstractTokenizer {

        public static CssInternalAttrSelectorTokenizer INSTANCE = new CssInternalAttrSelectorTokenizer();

        private QuotedValueTokenizer fQuotedValueTokenizer = new QuotedValueTokenizer();

        @Override
        protected StreamToken newToken() {
            return new CssSimpleAttrSelectorToken();
        }

        @Override
        public CssSimpleAttrSelectorToken read(CharStream stream) {
            CssSimpleAttrSelectorToken result = null;
            Marker marker = stream.markPosition();
            try {
                String nameToken = readName(stream);
                CssSimpleAttrSelectorToken.MatchType matchType = null;
                Pointer nameEnd = stream.getPointer();
                Pointer beginValue;
                Pointer endValue;
                if (nameToken != null) {
                    skipSpaces(stream);
                    char ch = stream.getChar();
                    switch (ch) {
                        case '~':
                            matchType = CssSimpleAttrSelectorToken.MatchType.INCLUDES;
                            break;
                        case '|':
                            matchType = CssSimpleAttrSelectorToken.MatchType.DASHMATCH;
                            break;
                        case '^':
                            matchType = CssSimpleAttrSelectorToken.MatchType.PREFIXMATCH;
                            break;
                        case '$':
                            matchType = CssSimpleAttrSelectorToken.MatchType.SUFFIXMATCH;
                            break;
                        case '*':
                            matchType = CssSimpleAttrSelectorToken.MatchType.SUBSTRINGMATCH;
                            break;
                        case '=':
                            matchType = CssSimpleAttrSelectorToken.MatchType.MATCH;
                            break;
                        default:
                            matchType = CssSimpleAttrSelectorToken.MatchType.NONE;
                            break;
                    }
                    beginValue = stream.getPointer();
                    endValue = beginValue;
                    if (matchType != CssSimpleAttrSelectorToken.MatchType.NONE) {
                        stream.incPos();
                        if (matchType != CssSimpleAttrSelectorToken.MatchType.MATCH) {
                            if (stream.getChar() == '=') {
                                stream.incPos();
                            }
                        }
                        skipSpaces(stream);
                        beginValue = stream.getPointer();
                        endValue = beginValue;
                        StreamToken quotedText = fQuotedValueTokenizer
                            .read(stream);
                        if (quotedText != null) {
                            beginValue = quotedText.getBegin();
                            endValue = quotedText.getEnd();
                        } else {
                            beginValue = stream.getPointer();
                            skipValue(stream);
                            endValue = stream.getPointer();
                        }
                    }
                    Pointer matchBegin = marker.getPointer();
                    Pointer matchEnd = (matchType != CssSimpleAttrSelectorToken.MatchType.NONE)
                        ? stream.getPointer()
                        : nameEnd;
                    String match = marker.getSubstring(matchBegin, matchEnd);
                    result = newToken(matchBegin, matchEnd, match);
                    result.setAttrName(nameToken);
                    result.setMatchType(matchType);
                    String value = marker.getSubstring(beginValue, endValue);
                    result.setMatchValue(value);
                }
            } finally {
                marker.close(result == null);
            }
            return result;
        }

    }

    public static class CssSimpleAttrSelectorToken extends CssAttrSelectorToken {

        public static enum MatchType {
            DASHMATCH("|="), INCLUDES("~="), MATCH("="), NONE(""), PREFIXMATCH(
                    "^="), SUBSTRINGMATCH("*="), SUFFIXMATCH("$=");
            private String fPattern;

            MatchType(String pattern) {
                fPattern = pattern;
            }

            @Override
            public String toString() {
                return fPattern;
            }

        }

        private String fAttrName;

        public MatchType fMatchType = MatchType.NONE;

        private String fMatchValue;

        public String getAttrName() {
            return fAttrName;
        }

        public MatchType getMatchType() {
            return fMatchType;
        }

        public String getMatchValue() {
            return fMatchValue;
        }

        public void setAttrName(String attrName) {
            fAttrName = attrName;
        }

        public void setMatchType(MatchType type) {
            fMatchType = type != null ? type : MatchType.NONE;
        }

        public void setMatchValue(String value) {
            fMatchValue = value;
        }

    }

    private static class CssSimpleAttrSelectorTokenizer
        extends
        AbstractTokenizer {

        public static CssSimpleAttrSelectorTokenizer INSTANCE = new CssSimpleAttrSelectorTokenizer();

        @Override
        protected StreamToken newToken() {
            return new CssSimpleAttrSelectorToken();
        }

        @Override
        public CssSimpleAttrSelectorToken read(CharStream stream) {
            CssSimpleAttrSelectorToken result = null;
            String attr = null;
            int valuePos = 1;
            MatchType matchType = CssSimpleAttrSelectorToken.MatchType.INCLUDES;
            char ch = stream.getChar();
            switch (ch) {
                case ':':
                    attr = ATTR_PSEUDO_CLASS_SELECTOR;
                    break;
                case '#':
                    matchType = CssSimpleAttrSelectorToken.MatchType.MATCH;
                    attr = ATTR_ID_SELECTOR;
                    break;
                case '.':
                    attr = ATTR_CLASS_SELECTOR;
                    break;
            }

            if (attr != null) {
                Marker marker = stream.markPosition();
                stream.incPos();
                try {
                    if (ATTR_PSEUDO_CLASS_SELECTOR.equals(attr)) {
                        if (stream.getChar() == ':') {
                            attr = ATTR_PSEUDO_ELEMENT_SELECTOR;
                            valuePos = 2;
                            stream.incPos();
                        }
                    }
                    if (skipValue(stream)) {
                        Pointer begin = marker.getPointer();
                        Pointer end = stream.getPointer();
                        int len = end.len(begin);
                        String str = marker.getSubstring(len);
                        result = newToken(begin, end, str);
                        result.setAttrName(attr);
                        result.setMatchType(matchType);
                        String value = str.substring(valuePos);
                        result.setMatchValue(value);
                    }
                } finally {
                    marker.close(result == null);
                }
            }
            return result;
        }
    }

    public static class CssTagSelectorToken extends StreamToken {

        private List<CssAttrSelectorToken> fAttributSelectors;

        private String fTagNameSelectorToken;

        public List<CssAttrSelectorToken> getAttributSelectors() {
            if (fAttributSelectors == null) {
                fAttributSelectors = new ArrayList<CssAttrSelectorToken>();
            }
            return fAttributSelectors;
        }

        public String getNameToken() {
            return fTagNameSelectorToken;
        }

        public void setAttributes(List<CssAttrSelectorToken> attrSelectors) {
            fAttributSelectors = attrSelectors;
        }

        public void setNameToken(String nameToken) {
            fTagNameSelectorToken = nameToken;
        }

    }

    public static class CssTagSelectorTokenizer extends AbstractTokenizer {

        @Override
        protected StreamToken newToken() {
            return new CssTagSelectorToken();
        }

        @Override
        public CssTagSelectorToken read(CharStream stream) {
            CssTagSelectorToken result = null;
            ArrayList<CssAttrSelectorToken> attributes = null;
            Pointer begin = stream.getPointer();
            String nameToken = readName(stream);
            while (true) {
                CssAttrSelectorToken token = readAttrSelectorToken(stream);
                if (token == null) {
                    break;
                }
                if (attributes == null) {
                    attributes = new ArrayList<CssAttrSelectorToken>();
                }
                attributes.add(token);
            }
            Pointer end = stream.getPointer();
            if (nameToken != null || attributes != null) {
                StringBuilder buf = new StringBuilder();
                if (nameToken != null) {
                    buf.append(nameToken);
                }
                if (attributes != null) {
                    for (CssAttrSelectorToken token : attributes) {
                        buf.append(token.getText());
                    }
                }
                result = newToken(begin, end, buf.toString());
                result.setNameToken(nameToken);
                result.setAttributes(attributes);
            }
            return result;
        }

        protected CssAttrSelectorToken readAttrSelectorToken(CharStream stream) {
            CssAttrSelectorToken token;
            token = CssSimpleAttrSelectorTokenizer.INSTANCE.read(stream);
            if (token == null) {
                token = CssCompositeAttrSelectorTokenizer.INSTANCE.read(stream);
            }
            return token;
        }

    }

    public static final String ATTR_CLASS_SELECTOR = "class";

    public static final String ATTR_COMPOSITE = "~composite";

    public static String ATTR_ID_SELECTOR = "id";

    public static final String ATTR_PSEUDO_CLASS_SELECTOR = ":class";

    public static final String ATTR_PSEUDO_ELEMENT_SELECTOR = ":element";

    public static final String COMBINATOR = "combinator";

    public static final String SELECTOR = "selector";

    private static boolean isValueChar(char ch, int pos) {
        return !Character.isSpaceChar(ch)
            && ch != '.'
            && ch != '#'
            && ch != '['
            && ch != ']';
    }

    private static String readIdent(CharStream stream) {
        char ch = stream.getChar();
        String result = null;
        if (ch == '*') {
            result = "*";
            stream.incPos();
        } else if (Character.isLetter(ch)) {
            StringBuilder buf = new StringBuilder();
            buf.append(ch);
            while (stream.incPos()) {
                ch = stream.getChar();
                if (!Character.isLetterOrDigit(ch)) {
                    break;
                }
                buf.append(ch);
            }
            result = buf.toString();
        }
        return result;
    }

    private static String readName(CharStream stream) {
        String result = null;
        Marker marker = stream.markPosition();
        try {
            String name = readIdent(stream);
            char ch = stream.getChar();
            if (ch == '|' && stream.incPos()) {
                name = readIdent(stream);
            }
            if (name != null) {
                Pointer begin = marker.getPointer();
                Pointer end = stream.getPointer();
                int len = end.len(begin);
                result = marker.getSubstring(len);
            }
            return result;
        } finally {
            marker.close(result == null);
        }
    }

    private static void skipSpaces(CharStream stream) {
        for (; Character.isSpaceChar(stream.getChar()); stream.incPos()) {
        }
    }

    private static boolean skipValue(CharStream stream) {
        boolean result = false;
        char ch = stream.getChar();
        int i = 0;
        while (isValueChar(ch, i)) {
            i++;
            result = true;
            if (!stream.incPos()) {
                break;
            }
            ch = stream.getChar();
        }
        return result;
    }

    public CssSelectorTokenizer() {
        addTokenizer(new CssTagSelectorTokenizer());
        addTokenizer(new CssCombinatorTokenizer());
    }

}
