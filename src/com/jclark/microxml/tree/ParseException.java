package com.jclark.microxml.tree;

/**
 * Signals an unrecoverable parsing error.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class ParseException extends RuntimeException {
    private final Location location;

    public ParseException(String message) {
        super(message);
        this.location = null;
    }

    public ParseException(Location location) {
        super();
        this.location = location;
    }

    public ParseException(String message, Location location) {
        super(message);
        this.location = location;
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.location = null;
    }

    /**
     * Returns the Location of the parsing error.
     * @return the Location of the parsing error; maybe null
     */
    public Location getLocation() {
        return location;
    }
}
