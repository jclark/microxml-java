package com.jclark.microxml.tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Contains methods parsing and serializing MicroXML.
 *
 * @see Element
 * @author James Clark
 */
public class MicroXML {
    private MicroXML() {}

    static public void serialize(Element element, Appendable appendable) throws IOException {
        SerializeUtil.serialize(element, appendable);
        appendable.append('\n');
    }

    static public void serialize(Element element, File file) throws IOException {
        Writer w = fileWriter(file);
        SerializeUtil.serialize(element, w);
        w.append('\n').close();
    }

    static private final int ATTRIBUTE_BUFFER_LENGTH = 16;

    static public void canonicalize(Element element, Appendable appendable) throws IOException {
        SerializeUtil.canonicalize(element, appendable, new Attribute[ATTRIBUTE_BUFFER_LENGTH]);
        appendable.append('\n');
    }

    static public void canonicalize(Element element, File file) throws IOException {
        Writer w = fileWriter(file);
        SerializeUtil.canonicalize(element, w, new Attribute[ATTRIBUTE_BUFFER_LENGTH]);
        w.append('\n').close();
    }

    static private Writer fileWriter(File file) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF8));
    }

    static public Element parse(String document, ParseOptions options) throws ParseException {
        Util.requireNonNull(options);
        LineMap lineMap = new LineMap(options.getURL());
        TreeBuilder treeBuilder = new TreeBuilder(lineMap, options);
        Tokenizer<ParseException> tokenizer = new Tokenizer<ParseException>(document, lineMap, treeBuilder);
        try {
            tokenizer.parse();
        }
        catch (IOException e) {
            throw new AssertionError();
        }
        return treeBuilder.getRoot();
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    static public Element parse(File file, ParseOptions options) throws ParseException, IOException {
        LineMap lineMap = new LineMap(file.toURI().toString());
        Reader reader = new InputStreamReader(new FileInputStream(file), UTF8);
        TreeBuilder treeBuilder = new TreeBuilder(lineMap, options);
        new Tokenizer<ParseException>(reader, lineMap, treeBuilder).parse();
        return treeBuilder.getRoot();
    }

    static public Element parse(Reader reader, ParseOptions options) throws ParseException, IOException {
        LineMap lineMap = new LineMap(options.getURL());
        TreeBuilder treeBuilder = new TreeBuilder(lineMap, options);
        new Tokenizer<ParseException>(reader, lineMap, treeBuilder).parse();
        return treeBuilder.getRoot();
    }

    static public Element parse(String document) throws ParseException {
        return parse(document, new ParseOptions());
    }
}
