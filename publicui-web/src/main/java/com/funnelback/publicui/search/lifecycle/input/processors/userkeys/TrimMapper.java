package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

/**
 * {@link AbstractTrimMapper} for v1 type key strings
 *
 */
public class TrimMapper extends AbstractTrimMapper {

    @Override
    protected KeyStringFormat getKeyStringFormat() {
        return KeyStringFormat.v1;
    }

}