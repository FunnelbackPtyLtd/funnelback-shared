package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

/**
 * {@link AbstractTrimMapper} for v1 type key strings
 *
 */
public class Trim2Mapper extends AbstractTrimMapper {

    @Override
    protected KeyStringFormat getKeyStringFormat() {
        return KeyStringFormat.v2;
    }

}
