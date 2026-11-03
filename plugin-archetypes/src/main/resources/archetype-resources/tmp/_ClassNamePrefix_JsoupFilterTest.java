package ${package};

import com.funnelback.common.filter.jsoup.MockJsoupFilterContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class _ClassNamePrefix_JsoupFilterTest {

    @Test
    public void testJsoupFilterPlugin() {
        MockJsoupFilterContext mockContext = new MockJsoupFilterContext(
            "<html>"
            + "<p>Hello this is an example document.</p>"
            + "</html>");
        
        new _ClassNamePrefix_JsoupFilter().processDocument(mockContext);

        Assertions.assertTrue(mockContext.getDocument().html().contains("</html>"));
    }
}