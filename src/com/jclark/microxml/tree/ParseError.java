package com.jclark.microxml.tree;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
enum ParseError {
    ISOLATED_SURROGATE("isolated surrogate"),
    INVALID_CODE_POINT("invalid code point"),
    UNESCAPED_GT("unescaped \"<\""),
    UNESCAPED_LT("unescaped \">\""),
    UNESCAPED_AMP("unescaped \"&\""),
    REF_CODE_POINT_TOO_BIG("character number must not exceed #x10FFFF"),
    MISSING_QUOTE("missing quote"),
    UNKNOWN_CHAR_NAME("reference to unknown character name"),
    UNTERMINATED_COMMENT("unterminated comment"),
    DOUBLE_MINUS_IN_COMMENT("comment must not contain \"--\""),
    TEXT_BEFORE_ROOT("text before root element"),
    CONTENT_AFTER_ROOT("text or elements after root element"),
    MISSING_END_TAG("missing end-tag"),
    EMPTY_DOCUMENT("empty document");

    private final String format;

    ParseError(String format) {
        this.format = format;
    }

    String format(Object... args) {
        return String.format(format, args);
    }
}
