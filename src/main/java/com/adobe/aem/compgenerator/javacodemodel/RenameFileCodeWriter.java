/*
 * #%L
 * AEM Component Generator
 * %%
 * Copyright (C) 2019 Adobe
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
package com.adobe.aem.compgenerator.javacodemodel;

import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class RenameFileCodeWriter extends FileCodeWriter {

    private final File target;

    public RenameFileCodeWriter(File target) throws IOException {
        super(target);
        this.target = target;
    }

    @Override
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        File file = this.getFile(pkg, fileName);
        RollbackFileHandler newFileAtPathAndRenameExisting = CommonUtils.getNewFileAtPathAndRenameExisting(file.getPath());
        return newFileAtPathAndRenameExisting.getFileOutputStreamNew();
    }

    @Override
    protected File getFile(JPackage pkg, String fileName) throws IOException {
        File dir;
        if (pkg.isUnnamed()) {
            dir = target;
        } else {
            dir = new File(target, pkg.name().replace('.', File.separatorChar));
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new File(dir, fileName);
    }
}
