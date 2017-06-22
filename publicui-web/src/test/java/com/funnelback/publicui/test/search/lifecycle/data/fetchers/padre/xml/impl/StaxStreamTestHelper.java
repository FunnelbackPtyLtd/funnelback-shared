package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class StaxStreamTestHelper {

    public static ResultPacket parse(File f) throws FileNotFoundException, XmlParsingException {
        return new StaxStreamParser().parse(
            new FileInputStream(f), StandardCharsets.UTF_8,
            false);
    }
}
