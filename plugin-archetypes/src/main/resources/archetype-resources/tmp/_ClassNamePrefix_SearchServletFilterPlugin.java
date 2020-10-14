package __fixed_package__;

import com.funnelback.plugin.servlet.filter.SearchServletFilterHook;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class _ClassNamePrefix_SearchServletFilterPlugin implements SearchServletFilterHook {

    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_SearchServletFilterPlugin.class);

    @Override
    public ServletRequest preFilterRequest(ServletRequest request) {
        log.trace(
                "Modify the servlet request; ServletFilterHook has other places " +
                        "where the request and response can be modified.");
        return request;
    }
}