package com.funnelback.publicui.utils.web;

import javax.servlet.ServletContext;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import com.funnelback.publicui.search.model.transaction.ExecutionContext;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Holds data tied to the execution context, such as the
 * context path ('/s') or if we're running as part of the
 * Public or Admin search interface
 */
@Log4j2
@Component
public class ExecutionContextHolder implements ServletContextAware {

    /**
     * URL where the webapp is deployed (Ex: <code>/s</code>).
     */
    @Getter private String contextPath;
    
    /**
     * Whether we serve the public search interface, or the
     * admin one.
     */
    @Getter private ExecutionContext executionContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.contextPath = servletContext.getContextPath();
        String servletExecutionContext = (String) servletContext.getAttribute(ExecutionContext.class.getSimpleName());
        try {
            this.executionContext = ExecutionContext.valueOf( servletExecutionContext );
        } catch (Exception e) {
            log.warn("Unknown execution context '"+servletExecutionContext+"'. Please set a valid '"+ExecutionContext.class.getSimpleName()+"' "
                    + "servlet attribute in your application server config (This is only needed for monitoring).");
            this.executionContext = ExecutionContext.Unknown;
        }
        log.info("Context path is '" + contextPath + "', execution context is '"+executionContext+"'");
    }

}
