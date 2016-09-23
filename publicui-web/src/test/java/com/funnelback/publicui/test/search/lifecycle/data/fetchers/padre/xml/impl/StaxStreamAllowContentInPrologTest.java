package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class StaxStreamAllowContentInPrologTest {

    private static String xml;
    
    @BeforeClass
    public static void beforeClass() throws IOException {
        FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml"), "UTF-8");
    }
    
    @Test(expected=XmlParsingException.class)
    public void testContentInPrologDisabled() throws XmlParsingException {
        StaxStreamParser parser = new StaxStreamParser();
        parser.parse(("Content in\nprolog<br>\r\n"+ xml).getBytes(),Charset.defaultCharset(), false);
    }

    @Test(expected=XmlParsingException.class)
    public void testContentInPrologEnabled() throws XmlParsingException {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(("Content in\nprolog\n"+ xml).getBytes(),Charset.defaultCharset(), true);
        assertNotNull(rp);
    }

}
