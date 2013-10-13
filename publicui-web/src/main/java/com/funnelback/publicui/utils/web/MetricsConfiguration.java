package com.funnelback.publicui.utils.web;

import static com.codahale.metrics.MetricRegistry.name;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.net.SocketFactory;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;

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
    
    /** JVM metrics namespace */
    public static final String JVM = "jvm";
    
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
    private ConfigRepository configRepository;
 
    private MetricRegistry registry = new MetricRegistry();
    
    private GraphiteReporter graphiteReporter;
    
    /**
     * Builds a new configuration and register JVM metrics on it
     */
    public MetricsConfiguration() {
        // Register JVM metrics
        registry.register(name(JVM, "file"), new FileDescriptorRatioGauge());
        registry.register(name(JVM, "gc"), new GarbageCollectorMetricSet());
        registry.register(name(JVM, "memory"), new MemoryUsageGaugeSet());
        registry.register(name(JVM, "thread"), new ThreadStatesGaugeSet());
    }
    
    /**
     * @return The {@link MetricRegistry} for the application, with configured
     * reporters
     */
    @Bean
    public MetricRegistry metricRegistry() {
        String graphiteHost = configRepository.getGlobalConfiguration().value(Keys.Metrics.GRAPHITE_HOST);
        int graphitePort = configRepository.getGlobalConfiguration().valueAsInt(
            Keys.Metrics.GRAPHITE_PORT, DefaultValues.Metrics.GRAHITE_PORT);
        
        if (graphiteHost != null) {
            String hostName = hostnameHolder.getHostname();
            if (hostName == null) {
                hostName = "unknown";
            } else {
                hostName = hostName.replace(".", "_").toLowerCase();
            }
        
            Graphite graphite = new Graphite(
                new InetSocketAddress(graphiteHost, graphitePort),
                SocketFactory.getDefault(), Charset.forName("US-ASCII"));
            graphiteReporter = GraphiteReporter.forRegistry(registry)
                .prefixedWith(hostName+"."+MODERNUI_PREFIX)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(graphite);

            graphiteReporter.start(1, TimeUnit.MINUTES);
        }

        return registry;
    }
    
    /**
     * Stop reporters
     */
    @PreDestroy
    public void preDestroy() {
        if (graphiteReporter != null) {
            graphiteReporter.stop();
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, registry);
    }
    
}
