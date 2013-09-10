package com.funnelback.publicui.search.service.index;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.ThreadSharedFileLock;
import com.funnelback.common.ThreadSharedFileLock.FileLockException;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;

/*
 * This Singelton should be used to lock the indexUpdate lock file while padre is run.
 * 
 * <p>This is used to ensure instant updates do not disrupt query processing. This class 
 * is easy to use and handles all the problems of multiple threads within the same JVM locking
 * on the same file.</p>
 */
public class IndexUpdateLock {


	@Autowired
    protected static I18n i18n;
	/*
	 * This holds all of the locks. At most there is one lock per collection. This is initially empty
	 * and locks must be added at most once per collection.
	 */
	private ConcurrentMap<String , ThreadSharedFileLock> lockMap;
	
	private IndexUpdateLock() {
		lockMap = new ConcurrentHashMap<String , ThreadSharedFileLock>();
	}
	
	private enum IndexUpdateLockSingelton {
		Instance;
		private static final IndexUpdateLock indexUpdateLock = new IndexUpdateLock();
		
		private IndexUpdateLock getIndexUpdateLock() {
			return indexUpdateLock;
		}
		
	}
	
	/*
	 * Get the only instance of this class
	 */
	public static IndexUpdateLock getIndexUpdateLockInstance() {
		return IndexUpdateLockSingelton.Instance.getIndexUpdateLock();
	}
	
	private String getKey(Config config) {
		return config.getCollectionName();
	}
	
	private String getCollectionUpdateLockFile(Config config) {
		return config.getCollectionRoot()
			 	+ File.separator + DefaultValues.VIEW_LIVE 
            	+ File.separator + DefaultValues.FOLDER_IDX
            	+ File.separator + Files.Index.UPDATE_LOCK;
	}
	
    public void lock(Config config) throws DataFetchException {
    	String key = getKey(config);
    	
    	//Ensure that the lockMap knows about this file to lock on.
    	//test if null before the putIfAbsdebt as we don't want to be creating a instance of the lock on every call
    	if (null == lockMap.get(key)){
    		//We must be atomic, so much can happen between the above IF statement and the below statement
    		lockMap.putIfAbsent(key, new ThreadSharedFileLock(getCollectionUpdateLockFile(config)));
    	}
    	
    	try {
			lockMap.get(key).lock();
		} catch (FileLockException e) {
			throw new DataFetchException(i18n.tr("padre.forking.lock.error"), e);
		}	
    }
    
    public void release(Config config) {
    	String key = getKey(config);
    	lockMap.get(key).release();
    }
}