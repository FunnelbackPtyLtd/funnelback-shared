package com.funnelback.publicui.search.lifecycle.input;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Base class for {@link InputProcessor}s
 * 
 * @since 12.4
 */
public abstract class AbstractInputProcessor implements InputProcessor {

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public abstract void processInput(SearchTransaction searchTransaction)
        throws InputProcessorException;

}
