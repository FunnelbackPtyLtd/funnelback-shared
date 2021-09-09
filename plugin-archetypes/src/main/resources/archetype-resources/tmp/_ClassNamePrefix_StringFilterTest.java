package __fixed_package__;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterResult;
import com.funnelback.filter.api.documents.StringDocument;
import com.funnelback.filter.api.filters.PreFilterCheck;
import com.funnelback.filter.api.mock.MockDocuments;
import com.funnelback.filter.api.mock.MockFilterContext;

public class _ClassNamePrefix_StringFilterTest {

    @Test
    public void testCanFilter(){
        MockFilterContext mockContext = MockFilterContext.getEmptyContext();
        StringDocument doc = MockDocuments.mockStringDoc("http://foo.com/",
                                            DocumentType.MIME_HTML_TEXT, 
                                            "hello");
        
        _ClassNamePrefix_StringFilter underTest = new _ClassNamePrefix_StringFilter();
        
        Assert.assertEquals("The filter will attempt to filter all documents including this one.",
            PreFilterCheck.ATTEMPT_FILTER, underTest.canFilter(doc, mockContext));
    }
    
    
    @Test
    public void testFilter(){
        MockFilterContext mockContext = MockFilterContext.getEmptyContext();
        StringDocument doc = MockDocuments.mockStringDoc("http://foo.com/", DocumentType.MIME_HTML_TEXT, "hello");
        
        _ClassNamePrefix_StringFilter underTest = new _ClassNamePrefix_StringFilter();
        
        FilterResult res = underTest.filter(doc, mockContext);
        
        Assert.assertEquals("Expect one document to be returned.", 1, res.getFilteredDocuments().size());
        
        Assert.assertTrue("The resulting document should be a StringDocument.",
            res.getFilteredDocuments().get(0) instanceof StringDocument);
        
        StringDocument resultingDocument = (StringDocument) res.getFilteredDocuments().get(0);
        
        Assert.assertEquals("hello", resultingDocument.getContentAsString());

    }
    
}
