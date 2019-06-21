/*
 * #%L
 * AEM Component Generator
 * %%
 * Copyright (C) 2019 Bounteous
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
    public PrologCodeWriter(CodeWriter core, String prolog ) {
        super(core);
        this.prolog = prolog;
    }

    @Override
    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        Writer w = super.openSource(pkg,fileName);

        PrintWriter out = new PrintWriter(w);
        // write prolog if this is a java source file
        if (prolog != null) {

            String s = prolog;
            int idx;
            while ((idx = s.indexOf('\n')) != -1) {
                out.println(s.substring(0, idx));
                s = s.substring(idx + 1);
            }
        }
        out.flush();
        return w;
    }
}
