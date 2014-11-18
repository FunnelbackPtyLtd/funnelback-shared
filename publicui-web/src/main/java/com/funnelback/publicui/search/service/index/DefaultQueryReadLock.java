package com.funnelback.publicui.search.service.index;

import org.springframework.stereotype.Component;

import com.funnelback.common.lock.ThreadSharedFileLock.FileLockException;
import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.model.collection.Collection;

@Component
public class DefaultQueryReadLock implements QueryReadLock{

	@Override
	public void lock(Collection collection) throws FileLockException {
	    Config config = collection.getConfiguration();
	    com.funnelback.common.lock.QueryReadLock.lock(config);
	}

	@Override
	public void release(Collection collection) {
	    Config config = collection.getConfiguration();
	    com.funnelback.common.lock.QueryReadLock.release(config);
	}
}
