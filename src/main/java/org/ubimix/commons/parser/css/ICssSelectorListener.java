package org.ubimix.commons.parser.css;

import org.ubimix.commons.parser.IParserListener;

/**
 * @author kotelnikov
 */
public interface ICssSelectorListener extends IParserListener {

    void beginAttributeGroup();

    void beginElement(String elementMask);

    void endAttributeGroup();

    void endElement(String elementMask);

    void onAttribute(
        String attributeName,
        String matchType,
        String matchValue);

    void onElementCombinator(char combinator);
}