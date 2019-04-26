package com.bounteous.aem.compgenerator.javacodemodel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FilterCodeWriter;

public class PrologCodeWriter extends FilterCodeWriter {

    /** prolog comment */
    private final String prolog;

    /**
     * @param core
     *      This CodeWriter will be used to actually create a storage for files.
     *      PrologCodeWriter simply decorates this underlying CodeWriter by
     *      adding prolog comments.
     * @param prolog
     *      Strings that will be added as comments
     *      and will be inserted at the beginning of each line to make it
     *      a valid Java comment.
     */
    public PrologCodeWriter( CodeWriter core, String prolog ) {
        super(core);
        this.prolog = prolog;
    }


    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        Writer w = super.openSource(pkg,fileName);

        PrintWriter out = new PrintWriter(w);

        // write prolog if this is a java source file
        if( prolog != null ) {

            String s = prolog;
            int idx;
            while( (idx=s.indexOf('\n'))!=-1 ) {
                out.println(s.substring(0,idx) );
                s = s.substring(idx+1);
            }
        }
        out.flush();    // we can't close the stream for that would close the undelying stream.

        return w;
    }
}
