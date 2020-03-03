package com.funnelback.filter.api.filters;

public enum PreFilterCheck {

    /**
     * Returned when the pre filter check decides that an attempt to filter the document should be made.
     */
    ATTEMPT_FILTER,
    /**
     * Returned when the pre filter check decides that the filter can not filter the given document and so the
     * filter should be skipped.
     */
    SKIP_FILTER
}
