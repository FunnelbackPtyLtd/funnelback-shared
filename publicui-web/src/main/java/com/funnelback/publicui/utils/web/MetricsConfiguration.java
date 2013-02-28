package com.funnelback.publicui.utils.web;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.GraphiteReporter;

/**
 * Configure the Yammer Metrics registry and corresponding
 * Graphite exporter if required.
 */
@Configuration
public class MetricsConfiguration {

	private static final String MODERNUI_PREFIX = "modernui";
	private static final String DEFAULT_REGISTRY = "defaultRegistry";
	
	/** Collection metric namespace */
	public static final String COLLECTION_NS = "collection";
	
	/** All/global namespace */
	public static final String ALL_NS = "all";
	
	/** View type (xml/json/html) namespace */
	public static final String VIEW_TYPE_NS = "view-type";
	
	public static final String UNKNOWN = "unknown";
	
	public static final String QUERIES = "queries";
	public static final String QUERIES_COUNT = QUERIES+"_count";
	public static final String ERRORS_COUNT = "errors_count";
	public static final String PADRE_ELAPSED_TIME = "padre-elapsed-time";
	public static final String TOTAL_MATCHING = "total-matching";
	
	@Autowired
	private LocalHostnameHolder hostnameHolder;
	
	@Autowired
	private ExecutionContextHolder executionContextHolder;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Bean
	public MetricsRegistry metricRegistry() {
		MetricsRegistry registry = new MetricsRegistry();
		
		String graphiteHost = configRepository.getGlobalConfiguration().value(Keys.Metrics.GRAPHITE_HOST);
		int graphitePort = configRepository.getGlobalConfiguration().valueAsInt(Keys.Metrics.GRAPHITE_PORT, DefaultValues.Metrics.GRAHITE_PORT);
		
		if (graphiteHost != null) {
			String hostName = hostnameHolder.getHostname();
			if (hostName == null) {
				hostName = "unknown";
			}
		
			GraphiteReporter.enable(registry, 10, TimeUnit.SECONDS,
					graphiteHost, graphitePort,
					hostName+"."+MODERNUI_PREFIX);
			
			GraphiteReporter.enable(10, TimeUnit.SECONDS,
					graphiteHost, graphitePort,
					hostName+"."+MODERNUI_PREFIX+"."+DEFAULT_REGISTRY);
		
			return registry;
		}
		
		return registry;
	}
	
}
