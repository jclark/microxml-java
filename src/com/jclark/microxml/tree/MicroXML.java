package com.jclark.microxml.tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
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
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
    }

    static public Element parse(String document) throws ParseException {
        LineMap lineMap = new LineMap(null);
        TreeBuilder treeBuilder = new TreeBuilder(lineMap);
        Tokenizer<ParseException> tokenizer = new Tokenizer<ParseException>(lineMap, document, treeBuilder);
        try {
            tokenizer.parse();
        }
        catch (IOException e) {
            throw new AssertionError();
        }
        return treeBuilder.getRoot();
    }
}
