package com.funnelback.publicui.recommender.compare;

import com.funnelback.publicui.recommender.Recommendation;
import org.apache.log4j.Logger;

import java.util.Comparator;

/**
 * MetaDataComparator is used to compare {@link Recommendation}s based on a given metadata field,
 * to allow them to be sorted. The sorting is done on a lexicographical basis.
 * @author fcrimmins@funnelback.com
 */

public final class MetaDataComparator implements Comparator<Recommendation> {
    private static final Logger logger = Logger.getLogger(MetaDataComparator.class);

    private String metadataClass;

    public MetaDataComparator(String metadataClass) {
        this.metadataClass = metadataClass;
    }

    public int compare(Recommendation o1, Recommendation o2) {
        String field1 = o1.getMetaData().get(metadataClass);
        String field2 = o2.getMetaData().get(metadataClass);

        if (field1 == null || field2 == null) {
            logger.warn("No " + metadataClass + " metadata available for one or more of: " + o1 + " " + o2);
            return Integer.MIN_VALUE;
        }

        return field2.compareTo(field1);
    }
}