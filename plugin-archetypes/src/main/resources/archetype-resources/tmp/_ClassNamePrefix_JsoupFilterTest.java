package __fixed_package__;

import org.junit.Assert;
import org.junit.Test;

import org.hamcrest.core.StringContains;

import com.funnelback.common.filter.jsoup.MockJsoupFilterContext;

public class _ClassNamePrefix_JsoupFilterTest {

    @Test
    public void testJsoupFilterPlugin() {
        MockJsoupFilterContext mockContext = new MockJsoupFilterContext(
            "<html>"
            + "<p>Hello this is an example document.</p>"
            + "</html>");
        
        new _ClassNamePrefix_JsoupFilter().processDocument(mockContext);
        
        Assert.assertThat(mockContext.getDocument().html(), StringContains.containsString("</html>"));
    }
    
}
