package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.Locator;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */

// TODO: this implementation is flawed because a SAX Locator gives us the position at the end of the Event
// TODO: probably not worth storing information about position of whitespace
class LocationBuildHandler extends BuildHandler {
    private Locator locator = null;
    private UrlList urlList = new UrlList();

    static class SimpleLocation extends AbstractLocation {
        private final String url;
        private final int lineNumber;
        private final int columnNumber;

        SimpleLocation(String url, int lineNumber, int columnNumber) {
            this.url = url;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        public String getURL() {
            return url;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }
    }

    static private class UrlList {
        @NotNull
        ArrayList<String> urls = new ArrayList<String>();

        int put(String url) {
            if (url == null)
                return -1;
            // TODO: cache last used index
            int i = urls.indexOf(url);
            if (i >= 0)
                return i;
            urls.add(url);
            return urls.size() - 1;
        }

        String get(int i) {
            if (i < 0)
                return null;
            return urls.get(i);
        }
    }

    static private class LocatedElement extends Element {
        static final int[] EMPTY_LOCATIONS = new int[0];
        @NotNull
        UrlList urlList;
        /**
         * Provides Locations for characters.
         * This maps from indices in the text array to Locations in the source text.
         * The length is a multiple of 4.
         * Each group of 4 integers is interpreted as follows:
         * <ol>
         *     <li>the index into the Element's text array</li>
         *     <li>the index into the uriList</li>
         *     <li>the line number</li>
         *     <li>the column number</li>
         * </ol>
         * In other words, each group is a mapping entry. The first integer in the group
         * is the map key, and the other three integers are the map value.
         */
        int[] charLocations = EMPTY_LOCATIONS;
        /**
         * Number of entries in charLocations.
         */
        int numCharLocations = 0;
        int startUrlIndex;
        int startLineNumber;
        int startColumnNumber;
        int endUrlIndex = -1;
        int endLineNumber = -1;
        int endColumnNumber = -1;

        LocatedElement(@NotNull String name, UrlList urlList, Locator locator) {
            super(name);
            this.urlList = urlList;
            startUrlIndex = urlList.put(locator.getSystemId());
            startLineNumber = locator.getLineNumber();
            startColumnNumber = locator.getColumnNumber();
        }

        void setEndTagLocation(Locator locator) {
            endUrlIndex = urlList.put(locator.getSystemId());
            endLineNumber = locator.getLineNumber();
            endColumnNumber = locator.getColumnNumber();
        }

        void addTextLocation(Locator locator) {
            String url = locator.getSystemId();
            int lineNumber = locator.getLineNumber();
            if (url == null && lineNumber < 0)
                return;
            if (numCharLocations == charLocations.length) {
                if (numCharLocations == 0)
                    charLocations = new int[16];
                else
                    charLocations = Arrays.copyOf(charLocations, numCharLocations * 2);

            }
            charLocations[numCharLocations++] = getTextLength();
            charLocations[numCharLocations++] = urlList.put(url);
            charLocations[numCharLocations++] = lineNumber;
            charLocations[numCharLocations++] = locator.getColumnNumber();
        }

        @Override
        public Location getStartTagLocation() {
            return new SimpleLocation(urlList.get(startUrlIndex), startLineNumber, startColumnNumber);
        }

        @Override
        public Location getEndTagLocation() {
            return new SimpleLocation(urlList.get(endUrlIndex), endLineNumber, endColumnNumber);
        }

        @Override
        public Location getTextLocation(int chunkIndex, int startIndexInChunk, int endIndexInChunk) {
            int startIndex = getAbsoluteStartIndex(chunkIndex, startIndexInChunk, endIndexInChunk);
            // TODO: use binary search
            // Find the last index in charLocations which is <= startIndex
            // Text changes may leave us with multiple mapping entries with the same index
            int i = numCharLocations;
            do {
                if (i == 0)
                    return null;
                i -= 4;
            } while (charLocations[i] > startIndex);
            if (charLocations[i] < startIndex)
                return null;
            return new SimpleLocation(urlList.get(charLocations[i + 1]),
                                      charLocations[i + 2],
                                      charLocations[i + 3]);
        }

        @Override
        protected void textChanging(int start, int end, int length) {
            int i = numCharLocations;
            int inc = length - (end - start);
            int prevLocationIndex = -1;
            for (;;) {
                if (i == 0)
                    return;
                i -= 4;
                int charIndex = charLocations[i];
                if (charIndex < end) {
                    // need to zap all locations that are >= start and < end
                    if (charIndex < start)
                        break;
                    if (prevLocationIndex == -1)
                        numCharLocations -= 4;
                    else {
                        // there is a subsequent mapping entry with the same index,
                        // so this one will get ignored
                        charLocations[i] = prevLocationIndex;
                        charLocations[i + 1] = -1;
                        charLocations[i + 2] = -1;
                        charLocations[i + 3] = -1;
                    }
                }
                else
                    charLocations[i] = prevLocationIndex = charIndex + inc;
            }
        }

        @Override
        void selfCheck() {
            super.selfCheck();
            assert numCharLocations <= charLocations.length;
            assert numCharLocations % 4 == 0;
            assert urlList != null;
            for (int i = 0; i < numCharLocations; i += 4) {
                assert charLocations[i] >= 0;
                assert charLocations[i] <= getTextLength();
                if (i > 0)
                    assert charLocations[i - 4] <= charLocations[i];
                assert charLocations[i + 1] >= -1;
                assert charLocations[i + 1] < urlList.urls.size();
                assert charLocations[i + 2] >= -1;
                assert charLocations[i + 3] >= - 1;
            }
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    protected Element createElement(String qName) {
        return locator == null ? super.createElement(qName) : new LocatedElement(qName, urlList, locator);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (locator != null)
            ((LocatedElement)currentElement).setEndTagLocation(locator);
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (length == 0)
            return;
        if (locator != null)
            ((LocatedElement)currentElement).addTextLocation(locator);
        super.characters(ch, start, length);
    }
}
