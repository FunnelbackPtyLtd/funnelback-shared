package com.funnelback.publicui.utils.web;

import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.funnelback.common.metric.MetricRegistryReporter;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.springmvc.utils.web.LocalHostnameHolder;

/**
 * Configure the Yammer Metrics registry and corresponding
 * Graphite exporter if required.
 */
@Component
public class MetricsConfiguration implements ServletContextAware {

    private static final String MODERNUI_PREFIX = "modernui";
    
    /** Collection metric namespace */
    public static final String COLLECTION_NS = "collection";
    
    /** All/global namespace */
    public static final String ALL_NS = "_all";
    
    /** View type (xml/json/html) namespace */
    public static final String VIEW_TYPE_NS = "view-type";
    
    /** Namespace to use for unknown collections and profiles */
    public static final String UNKNOWN = "_unknown";
    
    /** Metric name for queries */
    public static final String QUERIES = "queries";
    /** Metric name for errors */
    public static final String ERRORS_COUNT = "errors_count";
    /** Metric name for time spent by PADRE processing the query */
    public static final String PADRE_ELAPSED_TIME = "padre-elapsed-time";
    /** Metric name for total number of matching results */
    public static final String TOTAL_MATCHING = "total-matching";
    /** Metric name for cache requests */
    public static final String CACHE = "cache";
    /** Metric name for click requests */
    public static final String CLICK = "click";
    
    @Autowired
    private LocalHostnameHolder hostnameHolder;
    
    @Autowired
    private ExecutionContextHolder executionContextHolder;
    
    @Autowired
    private ConfigRepository configRepository;
 
    private MetricRegistry registry = new MetricRegistry();
    
    private MetricRegistryReporter registryReporter;
    
    /**
     * @return The {@link MetricRegistry} for the application, with configured
     * reporters
     */
    @Bean
    public MetricRegistry metricRegistry() {
        
        String hostName = hostnameHolder.getHostname();
        if (hostName == null) {
            hostName = "unknown";
        }
        String[] otherPrefixes = new String[2];
        otherPrefixes[0] = MODERNUI_PREFIX;
        otherPrefixes[1] = executionContextHolder.getExecutionContext().toString();
        registryReporter = com.funnelback.common.metric.MetricConfiguration.getConfiguredRegistryReporter(registry, 
            configRepository.getServerConfig(), 
            hostName, 
            otherPrefixes
        );

        return registry;
    }
    
    /**
     * Stop reporters
     */
    @PreDestroy
    public void preDestroy() {
        if (registryReporter != null) {
            try {
                registryReporter.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, registry);
    }
    
}
