package com.bounteous.aem.compgenerator.javacodemodel;

import com.bounteous.aem.compgenerator.utils.CommonUtils;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;

import java.io.File;
import java.io.IOException;

public class RenameFileCodeWriter extends FileCodeWriter {

    private final File target;

    public RenameFileCodeWriter(File target) throws IOException {
        super(target);
        this.target = target;
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

        String filePath = new File(dir, fileName).getPath();

        File codeFile = CommonUtils.getNewFileAtPathAndRenameExisting(filePath);

        return codeFile;
    }
}
