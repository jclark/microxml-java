package com.jclark.microxml.tree;

import java.util.EnumSet;

/**
 * Options that control MicroXML parsing.
 * @author James Clark
 */
public class ParseOptions {
    static private final ErrorHandler defaultErrorHandler =
            new ErrorHandler() {
                public void error(Location location, String message) throws ParseException {
                    throw new ParseException(message, location);
                }
            };
    private ErrorHandler errorHandler = defaultErrorHandler;
    private final EnumSet<ParseError> suppressedErrors = EnumSet.noneOf(ParseError.class);
    private String url;

    public ParseOptions() {
    }

    public ParseOptions(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public ParseOptions setErrorHandler(ErrorHandler errorHandler) {
        Util.requireNonNull(errorHandler);
        this.errorHandler = errorHandler;
        return this;
    }

    static public ErrorHandler getDefaultErrorHandler() {
        return defaultErrorHandler;
    }

    public String getURL() {
        return url;
    }

    /**
     * Sets the URL of the input stream being parsed.
     * This is used in reporting errors.
     * @param url the URL of the input stream being parsed.
     * @return a reference to this object
     * @see Location#getURL
     */
    public ParseOptions setURL(String url) {
        this.url = url;
        return this;
    }

    EnumSet<ParseError> getSuppressedErrors() {
        return suppressedErrors;
    }
}
