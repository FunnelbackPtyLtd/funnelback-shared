package ${package};

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        StringDocument doc = MockDocuments.mockStringDoc("http://foo.com/", DocumentType.MIME_HTML_TEXT, "hello");
        
        _ClassNamePrefix_StringFilter underTest = new _ClassNamePrefix_StringFilter();
        
        Assertions.assertEquals(PreFilterCheck.ATTEMPT_FILTER, underTest.canFilter(doc, mockContext),
            "The filter will attempt to filter all documents including this one");
    }

    @Test
    public void testFilter(){
        MockFilterContext mockContext = MockFilterContext.getEmptyContext();
        StringDocument doc = MockDocuments.mockStringDoc("http://foo.com/", DocumentType.MIME_HTML_TEXT, "hello");
        
        _ClassNamePrefix_StringFilter underTest = new _ClassNamePrefix_StringFilter();
        
        FilterResult res = underTest.filter(doc, mockContext);
        
        Assertions.assertEquals(1, res.getFilteredDocuments().size(),
            "Expect one document to be returned");

        Assertions.assertTrue(res.getFilteredDocuments().get(0) instanceof StringDocument,
            "The resulting document should be a StringDocument");

        StringDocument resultingDocument = (StringDocument) res.getFilteredDocuments().get(0);
        Assertions.assertEquals("hello", resultingDocument.getContentAsString());
    }
}