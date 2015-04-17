package com.funnelback.publicui.search.lifecycle.output.processors;

import static com.funnelback.publicui.utils.web.MetricsConfiguration.ALL_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.COLLECTION_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.ERRORS_COUNT;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.PADRE_ELAPSED_TIME;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.QUERIES;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.TOTAL_MATCHING;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.UNKNOWN;

import javax.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Update internal performance metrics
 */
@Component("metricsOutputProcessor")
@Log4j2
public class Metrics extends AbstractOutputProcessor {
    
    @Autowired
    @Setter private MetricRegistry metrics;
    
    /** Global count of search errors */
    private Counter allErrorsCounter;
    
    /** Average number of total matching results across all searches */
    private Histogram allTotalMatchingHistogram;
    
    /** Average time taken by padre across all searches */
    private Histogram allPadreElapsedTimeHistogram;
    
    /** Global number of queries processed */
    private Meter allQueriesMeter;
    
    @Override
    public void processOutput(SearchTransaction st) throws OutputProcessorException {
        allQueriesMeter.mark();
        
        String collectionAndProfile = UNKNOWN+"."+UNKNOWN;
        if (st != null) {
            if (st.hasQuestion() && SearchTransactionUtils.hasCollection(st)) {
                collectionAndProfile = st.getQuestion().getCollection().getId()
                        + "." + st.getQuestion().getProfile();
            }
        
            metrics.meter(MetricRegistry.name(COLLECTION_NS, collectionAndProfile, QUERIES)).mark();
        
            if (st.hasResponse()
                    && st.getResponse().hasResultPacket()) {
                
                if (st.getResponse().getResultPacket().getResultsSummary() != null) {
                    allTotalMatchingHistogram.update(st.getResponse().getResultPacket()
                        .getResultsSummary().getTotalMatching());
                    
                    metrics.histogram(MetricRegistry.name(COLLECTION_NS , collectionAndProfile, TOTAL_MATCHING))
                        .update(st.getResponse().getResultPacket().getResultsSummary().getTotalMatching());
                }
                
                if (st.getResponse().getResultPacket().getPadreElapsedTime() != null) {
                    allPadreElapsedTimeHistogram.update(st.getResponse().getResultPacket().getPadreElapsedTime());
                    
                    metrics.histogram(MetricRegistry.name(COLLECTION_NS , collectionAndProfile, PADRE_ELAPSED_TIME))
                        .update(st.getResponse().getResultPacket().getPadreElapsedTime());

                }

            }
        
            if (st.getError() != null) {
                allErrorsCounter.inc();
                metrics.counter(MetricRegistry.name(COLLECTION_NS, collectionAndProfile, ERRORS_COUNT)).inc();
            }
        }
    }
            
    
    @PostConstruct
    public void postConstruct() {
        allErrorsCounter = metrics.counter(MetricRegistry.name(ALL_NS, ERRORS_COUNT));
        allTotalMatchingHistogram = metrics.histogram(MetricRegistry.name(ALL_NS, TOTAL_MATCHING));
        allPadreElapsedTimeHistogram = metrics.histogram(MetricRegistry.name(ALL_NS, PADRE_ELAPSED_TIME));
        allQueriesMeter = metrics.meter(MetricRegistry.name(ALL_NS, QUERIES));
    }
    
    
}
