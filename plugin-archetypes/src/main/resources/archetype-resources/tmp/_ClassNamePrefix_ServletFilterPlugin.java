package __fixed_package__;

import com.funnelback.plugin.servlet.filter.ServletFilterHook;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class _ClassNamePrefix_ServletFilterPlugin implements ServletFilterHook {

    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_ServletFilterPlugin.class);

    @Override
    public ServletRequest preFilterRequest(ServletRequest request) {
        log.trace(
                "Modify the servlet request; ServletFilterHook has other places " +
                        "where the request and response can be modified.");
        return request;
    }
}
