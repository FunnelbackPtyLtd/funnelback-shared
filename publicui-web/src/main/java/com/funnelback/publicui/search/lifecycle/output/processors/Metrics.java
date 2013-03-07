package com.funnelback.publicui.search.lifecycle.output.processors;

import static com.funnelback.publicui.utils.web.MetricsConfiguration.ALL_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.COLLECTION_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.ERRORS_COUNT;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.PADRE_ELAPSED_TIME;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.QUERIES;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.TOTAL_MATCHING;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.UNKNOWN;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * Update internal performance metrics
 */
@Component("metricsOutputProcessor")
public class Metrics extends AbstractOutputProcessor {
    
    @Autowired
    @Setter private MetricsRegistry metrics;
    
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
        
            metrics.newMeter(new MetricName(COLLECTION_NS, collectionAndProfile, QUERIES), QUERIES, TimeUnit.SECONDS).mark();
        
            if (st.hasResponse()
                    && st.getResponse().hasResultPacket()) {
                
                if (st.getResponse().getResultPacket().getResultsSummary() != null) {
                    allTotalMatchingHistogram.update(st.getResponse().getResultPacket().getResultsSummary().getTotalMatching());
                    
                    metrics.newHistogram(new MetricName(COLLECTION_NS , collectionAndProfile, TOTAL_MATCHING), false)
                        .update(st.getResponse().getResultPacket().getResultsSummary().getTotalMatching());
                }
                
                if (st.getResponse().getResultPacket().getPadreElapsedTime() != null) {
                    allPadreElapsedTimeHistogram.update(st.getResponse().getResultPacket().getPadreElapsedTime());
                    
                    metrics.newHistogram(new MetricName(COLLECTION_NS , collectionAndProfile, PADRE_ELAPSED_TIME), false)
                        .update(st.getResponse().getResultPacket().getPadreElapsedTime());

                }

            }
        
            if (st.getError() != null) {
                allErrorsCounter.inc();
                metrics.newCounter(new MetricName(COLLECTION_NS, collectionAndProfile, ERRORS_COUNT)).inc();
            }
        }
    }
            
    
    @PostConstruct
    public void postConstruct() {
        allErrorsCounter = metrics.newCounter(new MetricName(ALL_NS, ALL_NS, ERRORS_COUNT));
        allTotalMatchingHistogram = metrics.newHistogram(new MetricName(ALL_NS, ALL_NS, TOTAL_MATCHING), false);
        allPadreElapsedTimeHistogram = metrics.newHistogram(new MetricName(ALL_NS, ALL_NS, PADRE_ELAPSED_TIME), false);
        allQueriesMeter = metrics.newMeter(new MetricName(ALL_NS, ALL_NS, QUERIES), QUERIES, TimeUnit.SECONDS);
    }
    
}
