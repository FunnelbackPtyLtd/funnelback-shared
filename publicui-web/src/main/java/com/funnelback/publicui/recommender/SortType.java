package com.funnelback.publicui.recommender;

import com.funnelback.publicui.recommender.compare.*;

import java.util.Collections;
import java.util.Comparator;

/**
 * Represents a sort type i.e. sorting based on recency, title etc.
 * @author fcrimmins@funnelback.com
 */
public class SortType {
    public final static String DEFAULT_METADATA_CLASS = "x";

    public static enum Type {
        recency("recency"),
        popularity("popularity"),
        title("title"),
        qie("qie"),
        metadata("metadata");

        private final String type;

        private Type(final String value) {
        	this.type = value;
		}

        @Override
        public String toString() {
            return type;
        }
    }

    public static enum Order {
        ascending("ascending"),
        descending("descending");

        private final String order;

        private Order(final String value) {
        	this.order = value;
		}

        @Override
        public String toString() {
            return order;
        }
    }

    public static enum Parameter {
        asort("asort"),
        dsort("dsort");

        private final String parameter;

        private Parameter(final String value) {
        	this.parameter = value;
		}

        @Override
        public String toString() {
            return parameter;
        }
    }

    /**
     * Return a comparator that can be used to sort based on the give type,
     * using the default ordering for that comparison (usually "descending").
     *
     * @param sortType sort type Enum.
     * @return comparator, or null if no suitable one exists
     */
    public static Comparator<Recommendation> getComparator(Type sortType) {
        return getComparator(sortType, Order.descending, DEFAULT_METADATA_CLASS);
    }

    /**
     * Return a comparator that can be used to sort based on the give type and order.
     *
     * @param sortType sort type Enum.
     * @param sortOrder sort order Enum.
     * @param metadataClass metadata class (may be null)
     * @return comparator, or null if no suitable one exists
     */
    public static Comparator<Recommendation> getComparator(Type sortType, Order sortOrder, String metadataClass) {
        Comparator<Recommendation> comparator = null;

        // Default order for all except "title" is descending
        switch(sortType) {
            case title:
                comparator = new TitleComparator();
                break;
            case recency:
                comparator = new DateComparator();
                break;
            case popularity:
                comparator = new PopularityComparator();
                break;
            case qie:
                comparator = new QIEComparator();
                break;
            case metadata:
                comparator = new MetaDataComparator(metadataClass);
                break;
        }

        if (sortOrder.equals(Order.ascending) && !sortType.equals(Type.title)) {
            comparator = Collections.reverseOrder(comparator);
        }
        else if (sortOrder.equals(Order.descending) && sortType.equals(Type.title)) {
            comparator = Collections.reverseOrder(comparator);
        }

        return comparator;
    }

    public static Comparator<Recommendation> getComparator(String asort, String dsort) throws Exception {
        return getComparator(asort, dsort, DEFAULT_METADATA_CLASS);
    }

    /**
     * Return an appropriate {@link Comparator} based on the given sort parameter values. If both values
     * are null and/or the empty string then a default "cooccurrence" based Comparator will be returned,
     * using descending order.
     *
     * If both parameters have a value then only the value of "dsort" will be respected. This method will
     * throw an exception if the sort type is not recognized.
     *
     * @param asort value for ascending sort
     * @param dsort value for descending sort
     * @param metadataClass metadata class (may be null)
     * @return {@link Comparator based on sort type and order}
     * @throws Exception
     */
    public static Comparator<Recommendation> getComparator(String asort, String dsort,
                                                           String metadataClass) throws Exception {
        Comparator<Recommendation> comparator = null;
        Type sortType;

        if (dsort != null && !("").equals(dsort)) {
            sortType = SortType.Type.valueOf(dsort);
            comparator = getComparator(sortType, Order.descending, metadataClass);
        }
        else if (asort != null && !("").equals(asort)) {
            sortType = SortType.Type.valueOf(asort);
            comparator = getComparator(sortType, Order.ascending, metadataClass);
        }

        return comparator;
    }
}
