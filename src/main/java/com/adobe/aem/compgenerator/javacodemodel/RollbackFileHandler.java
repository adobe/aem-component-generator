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

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class RollbackFileHandler {
    private static final Logger LOG = LogManager.getLogger(RollbackFileHandler.class);

    private final File fileNew;
    private final File fileOld;

    public RollbackFileHandler(File fileNew, File fileOld) {
        this.fileNew = fileNew;
        this.fileOld = fileOld;
    }

    public FileWriter getFileWriterNew() throws IOException {
        return (FileWriter) new RollbackFileWriter(fileNew, this);
    }

    public FileOutputStream getFileOutputStreamNew() throws IOException {
        return (FileOutputStream) new RollbackFileOutputStream(fileNew, this);
    }

    private void rollbackFile() {
        if (fileOld == null || fileNew == null) {
            LOG.debug("No rollback fileOld {} fileNew {}", fileOld, fileNew);
            return;
        }
        try {
            if (fileOld.isFile() && FileUtils.contentEquals(fileNew, fileOld)) {
                fileOld.delete();
                LOG.info("Rollback of unchanged file {}", fileOld.getPath());
            }
        } catch (IOException e) {
            LOG.error("Error on rollback, delete of old file failed", e);
        }
    }

    public static class RollbackFileWriter extends FileWriter {
        private final RollbackFileHandler rollbackFileHandler;

        private RollbackFileWriter(File file, RollbackFileHandler rollbackFileHandler) throws IOException {
            super(file);
            this.rollbackFileHandler = rollbackFileHandler;
        }

        public void close() throws IOException {
            super.close();
            rollbackFileHandler.rollbackFile();
        }
    }

    private class RollbackFileOutputStream extends FileOutputStream {
        private final RollbackFileHandler rollbackFileHandler;

        private RollbackFileOutputStream(File file, RollbackFileHandler rollbackFileHandler) throws IOException {
            super(file);
            this.rollbackFileHandler = rollbackFileHandler;
        }

        public void close() throws IOException {
            super.close();
            rollbackFileHandler.rollbackFile();
        }
    }
}
