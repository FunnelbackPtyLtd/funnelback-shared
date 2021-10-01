package com.funnelback.common.utils;

import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Files;

public class SharedXMLUtilsTest {

    private static final String UNICODE_CHARS = "é à ê ö";

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testWindows1252() throws Exception {
        byte[] xml = ("<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n" +
                "<breakfast_menu>\n" +
                "  <FOOD>\n" +
                "    <name>Belgian Waffles é à ê ö</name>\n" +
                "    <price>$5.95</price>\n" +
                "    <description>Two of our famous Belgian Waffles with plenty of real maple syrup</description>\n" +
                "    <calories>650</calories>\n" +
                "  </FOOD>\n" +
                "</breakfast_menu>\n").getBytes("windows-1252");
        Assert.assertFalse("test setup failure", new String(xml,"utf-8").contains(UNICODE_CHARS));
        Document doc = SharedXMLUtils.fromBytes(xml);
        String res = SharedXMLUtils.toString(doc);
        Assert.assertTrue(res.contains(UNICODE_CHARS));
        Assert.assertTrue(res.contains("encoding=\"UTF-8\""));
    }

    @Test
    public void testXXEVulnerability() throws Exception {
        File password = File.createTempFile(testName.getMethodName(),".pass").getAbsoluteFile();
        Files.writeString(password.toPath(), "los secretos en sus ojos");

        String doc=
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<!DOCTYPE svg [ <!ENTITY xxe SYSTEM \"file://" + password.getAbsolutePath() + "\">]>"
                        + "<xml><entry>First&xxe;</entry></xml>";

        String res = SharedXMLUtils.toString(SharedXMLUtils.fromString(doc));
        System.out.println(res);

        Assert.assertThat("", res, IsNot.not(StringContains.containsString("los secretos en sus ojos")));
    }

}
