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

    public ParseOptions() {
    }

    public ParseOptions(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        Util.requireNonNull(errorHandler);
        this.errorHandler = errorHandler;
    }

    static public ErrorHandler getDefaultErrorHandler() {
        return defaultErrorHandler;
    }

    EnumSet<ParseError> getSuppressedErrors() {
        return suppressedErrors;
    }
}
