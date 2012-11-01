package com.jclark.microxml.tree;

import java.util.Arrays;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
class TreeBuilder implements TokenHandler<ParseException> {

    static private final char BYTE_ORDER_MARK = 0xFEFF;

    private final LineMap lineMap;
    private LocatedElement root;
    private LocatedElement currentElement;
    private LocatedAttribute currentAttribute;
    private int expectedTextPosition = 0;

    TreeBuilder(LineMap lineMap) {
        this.lineMap = lineMap;
    }

    public void startTagOpen(int position, String name) throws ParseException {
        LocatedElement elem = new LocatedElement(name, position, lineMap);
        if (currentElement == null)
            root = currentElement;
        else
            currentElement.append(elem);
    }

    public void attributeOpen(int namePosition, int valuePosition, String name) throws ParseException {
        currentAttribute = new LocatedAttribute(name, namePosition, valuePosition, lineMap);
    }

    public void attributeClose() throws ParseException {
        currentElement.add(currentAttribute);
        currentAttribute = null;
    }

    public void startTagClose(int position) throws ParseException {
        // skip past ">"
        position++;
        currentElement.setStartTagCloseOffset(position);
        expectedTextPosition = position;
    }

    public void emptyElementTagClose(int position) throws ParseException {
        // skip past "/>"
        position += 2;
        currentElement.setStartTagCloseOffset(position);
        currentElement = (LocatedElement)(currentElement.getParent());
        expectedTextPosition = position;
    }

    public void endTag(int startPosition, int endPosition, String name) throws ParseException {
        currentElement.setEndTagOffsets(startPosition, endPosition);
    }

    public void literalChars(int position, char[] chars, int offset, int count) throws ParseException {
    }

    public void crLf(int position) throws ParseException {

    }

    public void charRef(int position, int refLength, char[] chars) throws ParseException {
    }

    public void end() throws ParseException {

    }

    public void error(int startPosition, int endPosition, String message) throws ParseException {
        fatal(startPosition, endPosition, message);
    }

    public void fatal(int startPosition, int endPosition, String message) throws ParseException {
        throw new ParseException(message, lineMap.getLocation(startPosition, endPosition));
    }

    /**
     * Provides functions and constants for dealing with text maps.
     * A text map is an array of integers that maps positions in text (parsed characters) to positions in source.
     * The array is partioned into segments
     * where each segment is a non-negative integer optionally followed by a
     * negative integer.  The non-negative integer is an index into text. The
     * information that is being provided about this index depends on the
     * following negative integer N (if any).  There are the following cases:
     * <ul>
     * <li>there is no following negative integer: the index is of an LF character that was the
     * result of parsing a CR/LF pair</li>
     * <li>N has the top two bits set (ie N in the range -1 to -0x4000_0000 inclusive):
     * there were -N characters of markup in the source that did not contribute to the text
     * (these can either be comments or elements that have been removed)</li>
     * <li>N has the top three bits of 100: the index is of a character that resulted from
     * a character reference; the remaining bits give the number of source characters in the character
     * reference</li>
     * <li>N has the top three bits of 101: the index is of the first character of a surrogate pair
     * that resulted from
     * a character reference; the remaining bits give the number of source characters in the character
     * reference</li>
     * </ul>
     */
    static class TextMap {
        static final int CONTINUE_FLAG       = 0x80000000;
        static final int MARKUP_FLAG         = 0x40000000;
        static final int SURROGATE_PAIR_FLAG = 0x20000000;
        static final int CHARREF_LENGTH_MASK = 0x1FFFFFFF;
        // if a text map is this, then there is a one-to-one mapping from text characters
        // to source characters
        static final int[] DIRECT = new int[0];
        // if a text map is this, there there is no information available about the mapping
        // from text characters to source characters
        static final int[] VOID = new int[0];

        /**
         *
         * @param startIndex index of start of range
         * @param endIndex index of end of range
         * @param baseIndex text index used as starting point
         * @param sourceOffset source offset corresponding to baseIndex
         * @param textMap array of integers specifying how to map from a text index to a source offset
         * @param textMapIndex index of first relevant entry in textMap
         * @param textMapLength used length of textMap
         * @param lineMap for mapping from a source offset to a line/column number
         * @return the Location of this range
         */
        static Location findLocation(int startIndex, int endIndex, int baseIndex, int sourceOffset,
                                     int[] textMap, int textMapIndex, int textMapLength, LineMap lineMap) {
            int startOffset = sourceOffset + (startIndex - baseIndex);
            int endOffset = sourceOffset + (endIndex - baseIndex);

            while (textMapIndex < textMapLength && textMap[textMapIndex] < baseIndex + endIndex) {
                int i = textMap[textMapIndex++];
                int extra;
                int n = 0;
                if (textMapIndex < textMapLength && (n = textMap[textMapIndex]) < 0) {
                    textMapIndex++;
                    if ((n & MARKUP_FLAG) != 0)
                        extra = -n;
                    else {
                        extra = (n & CHARREF_LENGTH_MASK);
                        if ((n & SURROGATE_PAIR_FLAG) != 0)
                            extra -= 2;
                        else
                            extra -= 1;
                    }
                }
                else
                    extra = 1;
                endOffset += extra;
                if (i < baseIndex + startIndex
                        || (i == baseIndex + startIndex
                        && n < 0
                        && (n & MARKUP_FLAG) != 0))
                    startOffset += extra;
            }
            return lineMap.getLocation(startOffset, endOffset);
        }

        /**
         * Returns the first non-negative integer i in the map such that textIndex <= textSourceMap[i]
         * and begin <= i < end;
         * end otherwise
         */
        static int getIndex(int textIndex, int[] textMap, int begin, int end) {
            int lower = begin;
            int upper = end - 1;
            while (lower <= upper) {
                // invariant: lower <= i <= upper
                if (lower == upper) {
                    if (textIndex <= textMap[lower])
                        return lower;
                    break;
                }
                assert lower < upper;
                int mid = lower + (upper - lower)/2;
                assert lower <= mid && mid < upper;
                int val = textMap[mid];
                if (val < 0) {
                    if (lower == mid) {
                        ++lower;
                        continue;
                    }
                    val = textMap[--mid];
                }
                assert val >= 0;
                assert lower <= mid && mid < upper;
                if (textIndex <= val)
                    upper = mid; // we're making progress since mid < upper
                else if (val >= 0)
                    lower = mid + 1;
            }
            return end;
        }
    }

    static final class LocatedElement extends Element {
        final LineMap lineMap;
        // index into source of first character of start-tag/empty-element-tag
        int startTagOpenOffset;
        int startTagCloseOffset;
        int endTagOpenOffset;
          // index into source after last character of end-tag/empty-element-tag
        int endTagCloseOffset;
        // number of entries in textSourceMap that are used
        int textSourceMapLength;

        int[] textSourceMap;

        LocatedElement(String name, int startOffset, LineMap lineMap) {
            super(name);
            this.startTagOpenOffset = startOffset;
            startTagCloseOffset = -1;
            endTagOpenOffset = -1;
            endTagCloseOffset = -1;
            this.lineMap = lineMap;
            textSourceMapLength = 0;
            textSourceMap = TextMap.DIRECT;
        }

        @Override
        public Location getStartTagLocation() {
            return lineMap.getLocation(startTagOpenOffset, startTagCloseOffset);
        }

        @Override
        public Location getEndTagLocation() {
            return lineMap.getLocation(endTagOpenOffset, endTagCloseOffset);
        }

        void setStartTagCloseOffset(int startTagCloseOffset) {
            this.startTagCloseOffset = startTagCloseOffset;
        }

        final void setEndTagOffsets(int openOffset, int closeOffset) {
            this.endTagOpenOffset = openOffset;
            this.endTagCloseOffset = closeOffset;
        }

        void addCharRef(char c, int length) {
            // TODO handle excessive length by adding a markup entry
            appendTextSourceMap(getTextLength());
            appendTextSourceMap(length | TextMap.CONTINUE_FLAG);
            append(c);
        }

        void addCharRef2(char c1, char c2, int length) {
            // TODO handle excessive length by adding a markup entry
            appendTextSourceMap(getTextLength());
            appendTextSourceMap(length|TextMap.CONTINUE_FLAG|TextMap.SURROGATE_PAIR_FLAG);
            append(c1).append(c2);
        }

        void noteComment(int length) {
            assert length > 0;
            // TODO if length is > MARKUP_FLAG, split into two
            appendTextSourceMap(getTextLength());
            appendTextSourceMap(-length);
        }

        void noteIgnoredLf() {
            appendTextSourceMap(getTextLength() - 1);
        }

        void appendTextSourceMap(int n) {
            if (textSourceMapLength >= textSourceMap.length) {
                if (textSourceMap.length == 0)
                    textSourceMap = new int[2];
                else
                    textSourceMap = Arrays.copyOf(textSourceMap, textSourceMap.length * 2);
            }
            textSourceMap[textSourceMapLength++] = n;
        }

        @Override
        public Location getTextLocation(int chunkIndex, int startIndex, int endIndex) {
            int textStartIndex = getTextChunkStartIndex(chunkIndex);
            int textEndIndex = getTextChunkEndIndex(chunkIndex);
            if (startIndex < 0 || endIndex < startIndex || endIndex > textEndIndex - textStartIndex)
                throw new IndexOutOfBoundsException();
            int sourceOffset;
            if (chunkIndex == 0)
                sourceOffset = startTagCloseOffset;
            else
                sourceOffset = ((LocatedElement)(children().get(chunkIndex - 1))).endTagCloseOffset;
            return TextMap.findLocation(textStartIndex + startIndex, textStartIndex + endIndex,
                                        textStartIndex, sourceOffset,
                                        textSourceMap,
                                        TextMap.getIndex(textStartIndex, textSourceMap, 0, textSourceMapLength),
                                        textSourceMapLength,
                                        lineMap);
        }
    }

    static class LocatedAttribute extends Attribute {
        final LineMap lineMap;
        int[] textMap;
        final int nameStartOffset;
        final int valueStartOffset;

        LocatedAttribute(String name, int nameStartOffset, int valueStartOffset, LineMap lineMap) {
            super(name, "");
            this.lineMap = lineMap;
            this.nameStartOffset = nameStartOffset;
            this.valueStartOffset = valueStartOffset;
            textMap = TextMap.DIRECT;
        }

        @Override
        Location getNameLocation() {
            return lineMap.getLocation(0, getName().length());
        }

        @Override
        Location getValueLocation(int beginIndex, int endIndex) {
            if (beginIndex < 0 || endIndex < beginIndex || endIndex > getValue().length())
                throw new IndexOutOfBoundsException();
            return TextMap.findLocation(beginIndex, endIndex, 0, valueStartOffset, textMap, 0, textMap.length, lineMap);
        }
    }


}
