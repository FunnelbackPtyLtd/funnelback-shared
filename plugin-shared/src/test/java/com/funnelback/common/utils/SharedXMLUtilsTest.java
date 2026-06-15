package com.funnelback.common.utils;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class SharedXMLUtilsTest {

    private static final String UNICODE_CHARS = "é à ê ö";

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
        Assertions.assertFalse(new String(xml,"utf-8").contains(UNICODE_CHARS), "test setup failure");
        Document doc = SharedXMLUtils.fromBytes(xml);
        String res = SharedXMLUtils.toString(doc);
        Assertions.assertTrue(res.contains(UNICODE_CHARS));
        Assertions.assertTrue(res.contains("encoding=\"UTF-8\""));
    }

    @Test
    public void testXXEVulnerability() throws Exception {
        File password = File.createTempFile("testXXEVulnerability",".pass").getAbsoluteFile();
        Files.writeString(password.toPath(), "los secretos en sus ojos");

        String doc=
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<!DOCTYPE svg [ <!ENTITY xxe SYSTEM \"file://" + password.getAbsolutePath() + "\">]>"
                        + "<xml><entry>First&xxe;</entry></xml>";

        // Security improvement: DOCTYPE declarations are now completely disabled
        // This test should throw an exception instead of processing the malicious XML
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SharedXMLUtils.fromString(doc);
        }, "XXE attack attempts should be rejected by disallowing DOCTYPE declarations");
    }
    
    @Test
    public void testMalformedXML() {
        String malformedXML = "<?xml version=\"1.0\"?><root><unclosed></root>";
        Assertions.assertThrows(Exception.class, () -> {
            SharedXMLUtils.fromString(malformedXML);
        });
    }
    
    @Test
    public void testEmptyXML() throws Exception {
        String emptyXML = "<?xml version=\"1.0\"?><root/>";
        Document doc = SharedXMLUtils.fromString(emptyXML);
        Assertions.assertNotNull(doc);
        String res = SharedXMLUtils.toString(doc);
        Assertions.assertNotNull(res);
        Assertions.assertTrue(res.contains("<root"));
    }
    
    @Test
    public void testLargeDocument() throws Exception {
        StringBuilder largeXML = new StringBuilder("<?xml version=\"1.0\"?><root>");
        for (int i = 0; i < 10000; i++) {
            largeXML.append("<item>").append(i).append("</item>");
        }
        largeXML.append("</root>");
        
        Document doc = SharedXMLUtils.fromString(largeXML.toString());
        Assertions.assertNotNull(doc);
        String res = SharedXMLUtils.toString(doc);
        Assertions.assertNotNull(res);
        Assertions.assertTrue(res.length() > 10000);
    }
    
    @Test
    public void testSSRFProtection() throws Exception {
        String ssrfXML = "<?xml version=\"1.0\"?><!DOCTYPE root [<!ENTITY xxe SYSTEM \"http://169.254.169.254/latest/meta-data/\">]><root>&xxe;</root>";
        
        // Security improvement: DOCTYPE declarations are now completely disabled
        // This test should throw an exception instead of processing the malicious XML
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SharedXMLUtils.fromString(ssrfXML);
        }, "SSRF attack attempts should be rejected by disallowing DOCTYPE declarations");
    }
    
    @Test
    public void testDoctypeDeclarationDisabled() throws Exception {
        // Test case to ensure DOCTYPE declarations are properly disabled to prevent XXE
        String doctypeXML = "<?xml version=\"1.0\"?><!DOCTYPE root [<!ELEMENT root ANY>]><root>test</root>";
        
        // This should throw an exception because DOCTYPE declarations should be disabled
        Assertions.assertThrows(Exception.class, () -> {
            SharedXMLUtils.fromString(doctypeXML);
        }, "DOCTYPE declarations should be disabled to prevent XXE attacks");
    }
}
