package ${package};

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.funnelback.plugin.servlet.filter.SearchServletFilterHook;
import com.funnelback.plugin.servlet.filter.SearchServletFilterHookContext;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class _ClassNamePrefix_SearchServletFilterPluginTest {

    @Test
    public void testSearchSerlvetFilterPlugin_PrefilterTest(){
        // Setup a mock servlet request
        HttpServletRequest mockServerHttpRequest = mock(HttpServletRequest.class);
        when(mockServerHttpRequest.getHeader(eq("x-foo"))).thenReturn("bar");

        // confirm mock behaviour
        Assertions.assertEquals("bar", mockServerHttpRequest.getHeader("x-foo"),
            "It should override default header value");

        // Update this to call the method(s) that should be tested. 
        HttpServletRequest updateRequest = (HttpServletRequest) new _ClassNamePrefix_SearchServletFilterPlugin()
            .preFilterRequest(mock(SearchServletFilterHookContext.class), mockServerHttpRequest);

        Assertions.assertEquals("bar", updateRequest.getHeader("x-foo"),
            "Change this assert to check something useful about preFilterRequest");
    }
}