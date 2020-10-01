package __fixed_package__;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.plugin.servlet.filter.ServletFilterHook;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class _ClassNamePrefix_ServletFilterPluginTest {

    @Test
    public void testSerlvetFilterPlugin_PrefilterTest(){

        // Setup a mock servlet request
        HttpServletRequest mockServerHttpRequest = mock(HttpServletRequest.class);
        when(mockServerHttpRequest.getHeader(eq("x-foo"))).thenReturn("bar");

        // confirm mock behaviour
        Assert.assertEquals("should override default header value",
                "bar", mockServerHttpRequest.getHeader("x-foo"));

        // Update this to call the method(s) that should be tested. 
        HttpServletRequest updateRequest =
                (HttpServletRequest) new _ClassNamePrefix_ServletFilterPlugin().preFilterRequest(mockServerHttpRequest);

        Assert.assertEquals("Change this assert to check something useful about preFilterRequest",
                "bar", updateRequest.getHeader("x-foo"));
    }
    
}
