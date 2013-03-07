package com.funnelback.publicui.search.service.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.springframework.stereotype.Component;

@Component
public class DefaultClickLogWriterHolder implements ClickLogWriterHolder {

    @Override
    public Writer getWriter(File logDir, String fileName) throws IOException {
        return new FileWriter(new File(logDir,fileName));
    }

}
