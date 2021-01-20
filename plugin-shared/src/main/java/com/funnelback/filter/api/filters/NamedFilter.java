package com.funnelback.filter.api.filters;

/**
 * Gives a name to filters for clearer logging.
 *
 */
public interface NamedFilter {

    /**
     * Gets the filters name.
     * 
     * <p>By default this will return the simple class name of the current instance.</p>
     * 
     * @return the name of the filer used for logging.
     */
    public default String getFilterName() {
        return this.getClass().getSimpleName();
    }
}
