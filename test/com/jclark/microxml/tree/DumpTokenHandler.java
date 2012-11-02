package com.jclark.microxml.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class DumpTokenHandler<E extends Throwable> implements TokenHandler<E> {
    StringBuilder out = new StringBuilder();
    public void startTagOpen(int position, String name) throws E {
        out.append('P').append(position).append('\n');
        out.append('(').append(name).append(' ').append('\n');
    }

    public void attributeOpen(int namePosition, int valuePosition, String name) throws E {
        out.append('P').append(namePosition).append('\n');
        out.append('A').append(name).append('\n');
        out.append('P').append(valuePosition).append('\n');
    }

    public void attributeClose() throws E {
        out.append('.').append('\n');
    }

    public void startTagClose(int position) throws E {
        out.append('P').append(position).append('\n');
        out.append('|').append('\n');
    }

    public void emptyElementTagClose(int position) throws E {
        out.append('P').append(position).append('\n');
        out.append(')').append('\n');
    }

    public void endTag(int startPosition, int endPosition, String name) throws E {
        out.append('P').append(startPosition).append(' ').append(endPosition).append('\n');
        out.append(')').append(name).append('\n');
    }

    public void literalChars(int position, char[] chars, int offset, int count) throws E {
        out.append('P').append(position).append('\n');
        int n = 0;
        for (int i = 0; i < count; i++)
            if (chars[offset + i] == '\n') {
                if (i > n)
                    out.append('-').append(chars, offset + n, i - n).append('\n');
                out.append('n').append('\n');
                n = i + 1;
            }
        if (count > n)
            out.append('-').append(chars, offset + n, count - n).append('\n');
    }

    public void charRef(int position, int refLength, char[] chars) throws E {
        out.append('P').append(position).append(' ').append(position + refLength).append('\n');
        if (chars[0] == '\n')
            out.append("N\n");
        else
            out.append('R').append(chars, 0, chars.length).append('\n');
    }

    public void crLf(int position) throws E {
        out.append('P').append(position).append('\n');
        out.append('r').append(position).append('\n');
    }

    public void end(int position) throws E {
       out.append('P').append(position).append('\n');
       out.append('$').append('\n');
    }

    public void error(int startPosition, int endPosition, String message) throws E {
       out.append('P').append(startPosition).append(' ').append(endPosition).append('\n');
       out.append('E').append(message).append('\n');
    }

    public void fatal(int startPosition, int endPosition, String message) throws E {
        out.append('P').append(startPosition).append(' ').append(endPosition).append('\n');
        out.append('F').append(message).append('\n');
    }

    static String loadFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream f = new FileInputStream(file);
        long length = file.length();
        if (length == 0)
            return "";
        if (length > Integer.MAX_VALUE)
            throw new IOException("file too big");
        byte[] buffer = new byte[(int)length];
        if (f.read(buffer, 0, buffer.length) != buffer.length)
            throw new IOException("read length");
        return new String(buffer, 0, buffer.length, "UTF-8");
    }

    static public void main(String[] args) throws IOException {
        DumpTokenHandler<RuntimeException> dumper = new DumpTokenHandler<RuntimeException>();
        Tokenizer<RuntimeException> tok = new Tokenizer<RuntimeException>(new LineMap(args[0]),
                                                                          loadFile(args[0]),
                                                                          dumper);
        tok.parse();
        System.out.print(dumper.out.toString());
    }
}
