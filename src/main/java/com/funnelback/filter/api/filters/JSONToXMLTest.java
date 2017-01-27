package com.funnelback.filter.api.filters;


import org.junit.Assert;
import org.junit.Test;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.FilterResult;
import com.funnelback.filter.api.documents.StringDocument;
import com.funnelback.filter.api.mock.MockDocuments;
import com.funnelback.filter.api.mock.MockFilterContext;
import com.funnelback.filter.filters.JSONToXML;

/*
 * Below are filter test methods. 
 */
public class JSONToXMLTest {
    
    @Test
    public void emptyJson() {
        String json = "{\n}\n";
        
        StringDocument doc = MockDocuments.mockEmptyStringDoc()
                                .cloneWithStringContent(DocumentType.fromContentType("application/json"), json);
        
        FilterResult res = new JSONToXML().filter(doc, MockFilterContext.getEmptyContext());
        
        String resultXml = StringDocument.from(res.getFilteredDocuments().get(0)).get().getContentAsString(); 
        
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            +"<json>\n"
            +"</json>", 
            resultXml);
    }
    
    @Test
    public void simpleJson() {
        String json = "{\"menu\": \"foo\"}";
        
        StringDocument doc = MockDocuments.mockEmptyStringDoc()
                                .cloneWithStringContent(DocumentType.fromContentType("application/json"), json);
        
        FilterResult res = new JSONToXML().filter(doc, MockFilterContext.getEmptyContext());
        
        String resultXml = StringDocument.from(res.getFilteredDocuments().get(0)).get().getContentAsString(); 
        
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            +"<json>"
            + "<menu>foo</menu>\n"
            +"</json>", 
            resultXml);
    }
    
    @Test
    public void complexExample() {
        String json = "{\n"
            +"    \"glossary\": {\n"
            +"        \"title\": \"example glossary\",\n"
            +"      \"GlossDiv\": {\n"
            +"            \"title\": \"S\",\n"
            +"          \"GlossList\": {\n"
            +"                \"GlossEntry\": {\n"
            +"                    \"ID\": \"SGML\",\n"
            +"                  \"SortAs\": \"SGML\",\n"
            +"                  \"GlossTerm\": \"Standard Generalized Markup Language\",\n"
            +"                  \"Acronym\": \"SGML\",\n"
            +"                  \"Abbrev\": \"ISO 8879:1986\",\n"
            +"                  \"GlossDef\": {\n"
            +"                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n"
            +"                      \"GlossSeeAlso\": [\"GML\", \"XML\"]\n"
            +"                    },\n"
            +"                  \"GlossSee\": \"markup\"\n"
            +"                }\n"
            +"            }\n"
            +"        }\n"
            +"    }\n"
            +"}\n";
        
        StringDocument doc = MockDocuments.mockEmptyStringDoc()
                                .cloneWithStringContent(DocumentType.fromContentType("application/json"), json);
        
        FilterResult res = new JSONToXML().filter(doc, MockFilterContext.getEmptyContext());
        
        String resultXml = StringDocument.from(res.getFilteredDocuments().get(0)).get().getContentAsString(); 
        
        String expectedXml ="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                +"<json>"
                +"<glossary>"
                +"<title>example glossary</title>"
                +"<GlossDiv>"
                +"<GlossList>"
                +"<GlossEntry>"
                +"<GlossTerm>Standard Generalized Markup Language</GlossTerm>"
                +"<GlossSee>markup</GlossSee>"
                +"<SortAs>SGML</SortAs>"
                +"<GlossDef>"
                +"<para>A meta-markup language, used to create markup languages such as DocBook.</para>"
                +"<GlossSeeAlso>GML</GlossSeeAlso>"
                +"<GlossSeeAlso>XML</GlossSeeAlso>"
                +"</GlossDef>"
                +"<ID>SGML</ID>"
                +"<Acronym>SGML</Acronym>"
                +"<Abbrev>ISO 8879:1986</Abbrev>"
                +"</GlossEntry>"
                +"</GlossList>"
                +"<title>S</title>"
                +"</GlossDiv>"
                +"</glossary>\n"
                +"</json>";
        
        Assert.assertEquals(expectedXml, resultXml);
    }
}