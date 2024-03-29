/**
 * 
 */
package org.ubimix.commons.parser.css;

import java.util.ArrayList;
import java.util.List;

import org.ubimix.commons.parser.AbstractParser;
import org.ubimix.commons.parser.ITokenizer;
import org.ubimix.commons.parser.StreamToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssAttrSelectorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssCombinatorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssCompositeAttrSelectorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssSimpleAttrSelectorToken;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssSimpleAttrSelectorToken.MatchType;
import org.ubimix.commons.parser.css.CssSelectorTokenizer.CssTagSelectorToken;

/**
 * @author kotelnikov
 */
public class CssSelectorParser extends AbstractParser<ICssSelectorListener>
    implements
    ICssSelectorParser {

    public CssSelectorParser() {
        this(CssSelectorTokenizer.INSTANCE);
    }

    public CssSelectorParser(ITokenizer tokenizer) {
        super(tokenizer);
    }

    @Override
    protected void doParse() {
        while (true) {
            StreamToken token = getToken(true);
            if (token == null) {
                break;
            }
            if (token instanceof CssCombinatorToken) {
                CssCombinatorToken t = (CssCombinatorToken) token;
                char type = t.getType();
                fListener.onElementCombinator(type);
            } else if (token instanceof CssTagSelectorToken) {
                CssTagSelectorToken t = (CssTagSelectorToken) token;
                String name = t.getNameToken();
                fListener.beginElement(name);
                List<CssAttrSelectorToken> attributes = t
                    .getAttributSelectors();
                notifyAttributes(attributes);
                fListener.endElement(name);
            }
        }
    }

    private void notifyAttributes(List<CssAttrSelectorToken> attributes) {
        for (CssAttrSelectorToken attr : attributes) {
            if (attr instanceof CssSimpleAttrSelectorToken) {
                CssSimpleAttrSelectorToken t = (CssSimpleAttrSelectorToken) attr;
                String name = t.getAttrName();
                MatchType matchType = t.getMatchType();
                String matchValue = t.getMatchValue();
                fListener.onAttribute(name, matchType.toString(), matchValue);
            } else if (attr instanceof CssCompositeAttrSelectorToken) {
                CssCompositeAttrSelectorToken t = (CssCompositeAttrSelectorToken) attr;
                fListener.beginAttributeGroup();
                List<CssAttrSelectorToken> list = new ArrayList<CssSelectorTokenizer.CssAttrSelectorToken>();
                list.addAll(t.getAttrSelectorTokens());
                notifyAttributes(list);
                fListener.endAttributeGroup();
            }
        }

    }

}