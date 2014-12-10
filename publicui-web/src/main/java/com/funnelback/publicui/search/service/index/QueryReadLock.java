package com.funnelback.publicui.search.service.index;

import com.funnelback.common.lock.ThreadSharedFileLock.FileLockException;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * This should be used to place a lock on the index during query
 *
 */
public interface QueryReadLock {

	/**
	 * Place a non-blocking shared lock on the index update lock file for a given collection.
	 * 
	 * <p>This will not give the caller a lock on the file, rather this call claims that a thread 
	 * needs to have a shared lock kept on the file for some time.</p>
	 * 
	 * <p>You must ensure that after each lock() call a matching call for release() will be executed. It is permitted 
	 * to call lock() multiple times as long as release is called a equal number of times. To ensure this the release
	 * must be called within a finally block.</p>
	 * 
	 * @param config Used to work out the collection and lock file to place the lock on.
	 * @throws FileLockException when the lock could not be acquired on the file.
	 */
	public void lock(Collection collection) throws FileLockException;

	/**
	 * Signify that the shared lock no longer needs to be kept on the index update lock file for a given collection.
	 * 
	 * <p> This should only be called after a call to lock() on the given collection. This should never be called more 
	 * than the number of previous lock() calls on a given collection.<p>
	 *  
	 * @param config Used to work out the collection and lock file.
	 */
	public void release(Collection collection);

}
