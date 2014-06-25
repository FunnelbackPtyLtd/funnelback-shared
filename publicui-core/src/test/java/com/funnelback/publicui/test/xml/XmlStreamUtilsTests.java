package com.funnelback.publicui.test.xml;

import java.io.StringReader;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.xml.XmlStreamUtils;

public class XmlStreamUtilsTests {

    private static final String XML =
        "<root>"
        + "<child1>"
        + "<a>value A</a><b>value B</b>"
        + "</child1>"
        + "<child2>"
        + "<c>value C</c>"
        + "</child2>"
        + "<comma_sep>"
        + "2,3,4"
        + "</comma_sep>"
        + "</root>";
    
    @Test
    public void test() throws XMLStreamException, FactoryConfigurationError {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(XML));
        
        xmlStreamReader.nextTag(); // Advance to "<root>"
        xmlStreamReader.nextTag(); // Now up to "<child1>"
        
        Map<String, String> out = null;
        
        try {
            //Sitting at "<child1>"; fail to turn "bad" subtree into a map
            out = XmlStreamUtils.tagsToMap("bad", xmlStreamReader);
            Assert.fail("Should fail with an invalid tag name");
        } catch (IllegalArgumentException iae) {}
        
        //Sitting at "<child1>"; successfully turn "child1" subtree into a map
        out = XmlStreamUtils.tagsToMap("child1", xmlStreamReader);
        Assert.assertEquals(2, out.size());
        Assert.assertEquals("value A", out.get("a"));
        Assert.assertEquals("value B", out.get("b"));
        
        try {
            //Now sitting at "</child1>"; fail to turn the "child1" subtree into a map
            XmlStreamUtils.tagsToMap("child1", xmlStreamReader);
            Assert.fail("Should fail if not on a start element");
        } catch(IllegalArgumentException iae) { }
        
        //Advance to "<child2>"
        xmlStreamReader.nextTag();        
        //successfully turn "child2" subtree into map
        out = XmlStreamUtils.tagsToMap("child2", xmlStreamReader);
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("value C", out.get("c"));
    
        //Advance to "<comma_sep>"
        xmlStreamReader.nextTag();
        //successfully read tag contents"
        Assert.assertEquals("2,3,4", xmlStreamReader.getElementText());
    }
    
}
