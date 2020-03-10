package com.funnelback.filter.api.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterResult;
import com.funnelback.filter.api.documents.BytesDocument;
import com.funnelback.filter.api.documents.FilterableDocument;
import com.funnelback.filter.api.documents.NoContentDocument;
import com.funnelback.filter.api.documents.StringDocument;
import com.funnelback.filter.api.filters.BytesDocumentFilter;
import com.funnelback.filter.api.filters.PreFilterCheck;
import com.funnelback.filter.api.filters.StringDocumentFilter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public class MockDocumentsTest {
    
    @Test
    public void checkEmptyDocumentsAreEqual() {
        Assert.assertEquals(MockDocuments.mockEmptyByteDoc(), MockDocuments.mockEmptyByteDoc());
        Assert.assertEquals(MockDocuments.mockEmptyStringDoc(), MockDocuments.mockEmptyStringDoc());
    }
   
    /*
     * Below are silly filters with an example of how they might be tested. 
     *
     */
    
    public class UTF8BytesToStringFilter implements BytesDocumentFilter {

        @Override
        public PreFilterCheck canFilter(NoContentDocument document, FilterContext context) {
            if(document.getDocumentType().asContentType().startsWith("text/crazyutf8")){
                return PreFilterCheck.ATTEMPT_FILTER;
            }
            return PreFilterCheck.SKIP_FILTER;
        }

        @Override
        public FilterResult filterAsBytesDocument(BytesDocument document, FilterContext context) {
            String s = new String(document.getCopyOfContents(), StandardCharsets.UTF_8);
            
            return FilterResult.of(context.getFilterDocumentFactory().toStringDocument(document, context.getDocumentTypeFactory().fromContentTypeHeader("text/text"), s));
        }
        
    }
    
    @Test
    public void UTF8BytesToStringFilterTest() throws Exception {
        String s = "Can you deal with the real stuff: 日本 Throwing some French accents in the mix: é à ê ö";
        
        //Create the dummy input document.
        
        BytesDocument inputDoc = MockDocuments.mockByteDoc("http://foo.com/", 
                                                                new MockDocumentType().withContentType("text/crazyutf8"), 
                                                                Optional.empty(), 
                                                                s.getBytes("UTF-8"));
        
        MockFilterContext context = MockFilterContext.getEmptyContext();
        
        Assert.assertEquals(PreFilterCheck.ATTEMPT_FILTER, new UTF8BytesToStringFilter().canFilter(inputDoc, context));
        
        StringDocument result = (StringDocument) new UTF8BytesToStringFilter()
                                                    .filterAsBytesDocument(inputDoc, context)
                                                    .getFilteredDocuments()
                                                    .get(0);
        
        Assert.assertTrue(s.equals(result.getContentAsString()));
    }
    
    public class ConfigReadingHeaderSettingFilter implements StringDocumentFilter {

        @Override
        public PreFilterCheck canFilter(NoContentDocument document, FilterContext context) {
            return PreFilterCheck.ATTEMPT_FILTER;
        }

        @Override
        public FilterResult filterAsStringDocument(StringDocument document, FilterContext context) {
            
            ListMultimap<String, String> newMetadata = document.getCopyOfMetadata();
            
            newMetadata.removeAll("confvalue");
            newMetadata.put("confvalue", context.getConfigValue("key").orElse("default-value"));
            
            newMetadata.removeAll("collname");
            newMetadata.put("collname", context.getCollectionName());
            
            StringDocument newDoc = document.cloneWithMetadata(newMetadata);
            return FilterResult.of(newDoc);
        }
    }
    
    
    @Test
    public void ConfigReadingHeaderSettingFilterTest() throws Exception {
        
        
        StringDocument inputDoc = MockDocuments.mockStringDoc("http://foo.com/", DocumentType.MIME_UNKNOWN, "content");
        
        ListMultimap<String, String> map = inputDoc.getCopyOfMetadata();
        map.put("existing", "value");
        inputDoc = inputDoc.cloneWithMetadata(map);
        
        MockFilterContext context = MockFilterContext.getEmptyContext();
        context.setCollectionName("coll1");
        context.setConfigValue("key", "my value");
        
        Assert.assertEquals(PreFilterCheck.ATTEMPT_FILTER, new ConfigReadingHeaderSettingFilter().canFilter(inputDoc, context));
        
        StringDocument result = (StringDocument) new ConfigReadingHeaderSettingFilter()
                                                    .filterAsStringDocument(inputDoc, context)
                                                    .getFilteredDocuments()
                                                    .get(0);
        
        StringDocument expected = MockDocuments.mockStringDoc("http://foo.com/", DocumentType.MIME_UNKNOWN, "content")
            .cloneWithMetadata(ImmutableListMultimap.of("existing", "value", "confvalue", "my value", "collname", "coll1"));
        
        Assert.assertEquals("We expected: \n" + expected.toString() +"\n but got: \n" + result,
            expected, result);
    }
    
    
    public class DeleteNonHTML implements StringDocumentFilter {

        @Override
        public PreFilterCheck canFilter(NoContentDocument document, FilterContext context) {
            if(document.getDocumentType().isHTML()) {
                return PreFilterCheck.SKIP_FILTER;
            }
            return PreFilterCheck.ATTEMPT_FILTER;
        }

        @Override
        public FilterResult filterAsStringDocument(StringDocument document, FilterContext context) {
            //Only called if the document is not HTML
            return FilterResult.delete();
        }
    }
    
    @Test
    public void DeleteNonHTMLTest() {
        //First test htmlDoc
        StringDocument htmlDoc = MockDocuments.mockStringDoc("http://foo.com/", 
                                                                    DocumentType.MIME_HTML_TEXT, 
                                                                    "<html><body><p>hi</p></body></html>");
        
        MockFilterContext context = MockFilterContext.getEmptyContext();
        Assert.assertEquals(PreFilterCheck.SKIP_FILTER, new DeleteNonHTML().canFilter(htmlDoc, context));
        
        StringDocument xmlDoc = MockDocuments.mockStringDoc("http://foo.com/", 
                                                                    DocumentType.MIME_XML_TEXT, 
                                                                    "<html><body><p>hi</p></body></html>");
        
        Assert.assertEquals(PreFilterCheck.ATTEMPT_FILTER, new DeleteNonHTML().canFilter(xmlDoc, context));
        
        Assert.assertTrue(new DeleteNonHTML().filterAsStringDocument(xmlDoc, context).getFilteredDocuments().isEmpty());
    }
    
    public class FixURLFilter implements StringDocumentFilter {

        @Override
        public PreFilterCheck canFilter(NoContentDocument document, FilterContext context) {
            if(document.getURI().getFragment() != null) {
                return PreFilterCheck.ATTEMPT_FILTER;
            }
            return PreFilterCheck.SKIP_FILTER;
        }

        @Override
        public FilterResult filterAsStringDocument(StringDocument document, FilterContext context) {
            URI originalURI = document.getURI();
            try {
                String fragmentInQuery = "funfrag="+document.getURI().getFragment();
                //Construct a new URI, based of the old one placing the fragment into the query
                URI newURI = new URI(document.getURI().getScheme(), 
                    document.getURI().getUserInfo(), 
                    document.getURI().getHost(), 
                    document.getURI().getPort(), 
                    document.getURI().getPath(), 
                    Optional.ofNullable(document.getURI().getQuery()).map(q -> q + "&" + fragmentInQuery).orElse(fragmentInQuery), 
                    null); //null for empty fragment
                return FilterResult.of(document.cloneWithURI(newURI));
            } catch (Exception e) {
                //we couldn't fix the URI just hope for the best
                return FilterResult.skipped();
            }
        }
    }
    
    @Test
    public void FixURLFilterTest() throws Exception {
        StringDocument inputDoc = MockDocuments.mockStringDoc("http://www.example.org/foo.html#bar", DocumentType.MIME_TEXT_PLAIN, "content");
        MockFilterContext context = MockFilterContext.getEmptyContext();
        
        Assert.assertEquals(PreFilterCheck.ATTEMPT_FILTER, new FixURLFilter().canFilter(inputDoc, context));
        
        StringDocument result = (StringDocument) new FixURLFilter().filterAsStringDocument(inputDoc, context).getFilteredDocuments().get(0);
        
        String resultURL = result.getURI().toASCIIString();
        
        Assert.assertEquals("http://www.example.org/foo.html?funfrag=bar", resultURL);
    }
    
    public class SplitDocumentFilter implements StringDocumentFilter {

        @Override
        public PreFilterCheck canFilter(NoContentDocument document, FilterContext context) {
            //Don't split xml
            if(document.getDocumentType().isXML()) {
                return PreFilterCheck.SKIP_FILTER;
            }
            
            //Don't split json
            if(document.getDocumentType().isJSON()) {
                return PreFilterCheck.SKIP_FILTER;
            }
            
            return PreFilterCheck.ATTEMPT_FILTER;
        }

        @Override
        public FilterResult filterAsStringDocument(StringDocument document, FilterContext context) {
            Optional<String> delim = context.getConfigValue("X-SplitDocumentFilter.delim");
            if(delim.isPresent()) {
                String delimiter = delim.get();
                String docContent = document.getContentAsString();
                if(!docContent.contains(delimiter)) {
                    //Document can not be split return it as is.
                    return FilterResult.of(document);
                }
                
                List<StringDocument> newDocs = new ArrayList<>();
                for(String part : docContent.split(Matcher.quoteReplacement(delimiter))) {
                    
                    try {
                        //A new URI, use UUID to ensure the id is unique
                        //We should see if the document can tell us the name of the sub documents.
                        URI newURI = new URI("http://" + document.getURI().getHost() + "/books/" + UUID.randomUUID());
                        newDocs.add(document.cloneWithURI(newURI).cloneWithStringContent(document.getDocumentType(), part));
                    } catch (URISyntaxException e) {
                        //Pretty sure this wont happen
                        e.printStackTrace();
                    }
                }
                
                //Return our new documents
                return FilterResult.of(newDocs);
            } else {
                //No one has set the delimiter so skip this filter.
                return FilterResult.skipped();
            }
        }
    }
    
    @Test
    public void testSplitDocumentFilter() {
        StringDocument inputDoc = MockDocuments.mockStringDoc("http://domain.com/", 
                                                            DocumentType.MIME_HTML_TEXT, 
                                                            "doc1SEPdoc2SEPdoc3");
        
        MockFilterContext context = MockFilterContext.getEmptyContext();
        context.setConfigValue("X-SplitDocumentFilter.delim", "SEP");
        
        Assert.assertEquals(PreFilterCheck.ATTEMPT_FILTER, new SplitDocumentFilter().canFilter(inputDoc, context));
        
        List<FilterableDocument> results = new SplitDocumentFilter().filterAsStringDocument(inputDoc, context).getFilteredDocuments();
        
        //We should have three documents.
        Assert.assertTrue(3 == results.size());
        
        //Check the doc content
        Assert.assertTrue("doc1".equals(((StringDocument) results.get(0)).getContentAsString()));
        Assert.assertTrue("doc2".equals(((StringDocument) results.get(1)).getContentAsString()));
        Assert.assertTrue("doc3".equals(((StringDocument) results.get(2)).getContentAsString()));
        
        //Now check the URLs are unique
        Set<String> urls = new HashSet<>();
        urls.add(results.get(0).getURI().toASCIIString());
        urls.add(results.get(1).getURI().toASCIIString());
        urls.add(results.get(2).getURI().toASCIIString());
        
        //Should have 3 URLs in our set.
        Assert.assertTrue(3 == urls.size());
        
    }
    
}
