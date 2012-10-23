package com.jclark.microxml.tree;

/**
 * A line and column number. The first line-number is 1. The first column of a line is 1. Line and column numbers are
 * clamped Integer.MAX_VALUE: values greater than Integer.MAX_VALUE are represented by Integer.MAX_VALUE. Lines are
 * considered to be delimited by \n, \r, or \r\n only. The column number of a character is one plus the number of Java
 * chars (16-bit UTF-16 code units) preceding it on the line. This will not always correspond to the column number in a
 * text editor.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class LinePosition {
    private final int lineNumber;
    private final int columnNumber;

    static public final LinePosition VOID = new LinePosition(-1, -1);

    public LinePosition(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
