package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;

/**
 * {@link Executor} for PADRE that considers all return codes
 * as being succesful. PADRE will return a non-zero code if an error
 * occured, but should still returns a valid XML packet in most cases.
 * 
 * We need this special {@link Executor} to avoid throwing an Exception
 * if the return code is non-zero.
 */
public class PadreExecutor extends DefaultExecutor {

	@Override
	public boolean isFailure(int result) {
		return false;
	}
	
}
