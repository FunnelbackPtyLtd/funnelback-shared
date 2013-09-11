package com.funnelback.publicui.search.service.index;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.ThreadSharedFileLock;
import com.funnelback.common.ThreadSharedFileLock.FileLockException;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;

@Component
public class DefaultQueryReadLock implements QueryReadLock{

	@Override
	public void lock(Collection collection) throws FileLockException {
		DefaultQueryReadLockSingleton.getDefaultQueryReadLockSingleton().lock(collection);
		
	}

	@Override
	public void release(Collection collection) {
		DefaultQueryReadLockSingleton.getDefaultQueryReadLockSingleton().release(collection);
	}
	
	/**
	 * This Singleton should be used to lock the indexUpdate lock file while padre is run.
	 * 
	 * <p>This is used to ensure instant updates do not disrupt query processing. This class 
	 * is easy to use and handles all the problems of multiple threads within the same JVM locking
	 * on the same file.</p>
	 *
	 */
	public static class DefaultQueryReadLockSingleton {

		@Autowired
	    protected static I18n i18n;
		/*
		 * This holds all of the locks. At most there is one lock per collection. This is initially empty
		 * and locks must be added at most once per collection.
		 */
		private ConcurrentMap<String , ThreadSharedFileLock> lockMap = new ConcurrentHashMap<String , ThreadSharedFileLock>();
		
		private DefaultQueryReadLockSingleton() { }
		
		/*
		 * Get the only instance of this class
		 */
		@Getter
		private static final DefaultQueryReadLockSingleton defaultQueryReadLockSingleton = new DefaultQueryReadLockSingleton();
		
		private String getKey(Config config) {
			return config.getCollectionName();
		}
		
		private String getCollectionUpdateLockFile(Config config) {
			return config.getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE 
					+ File.separator + DefaultValues.FOLDER_IDX
					+ File.separator + Files.Index.UPDATE_LOCK;
		}
		
	    public void lock(Collection collection) throws FileLockException {
	    	Config config = collection.getConfiguration();
	    	String key = getKey(config);
	    	
	    	//Ensure that the lockMap knows about this file to lock on.
	    	//Without this IF we would have to create a new instance of ThreadSharedFileLock even if it was already in the
	    	//map on every single call. Keep in mind this is called for every query. 
	    	if (null == lockMap.get(key)){
	    		//We must be atomic, another thread might have completed the above IF at the same time or just before 
	    		//us
	    		lockMap.putIfAbsent(key, new ThreadSharedFileLock(getCollectionUpdateLockFile(config)));
	    	}
	    	
	    	lockMap.get(key).lock();
	    }
	    
	    public void release(Collection collection) {
	    	Config config = collection.getConfiguration();
	    	String key = getKey(config);
	    	lockMap.get(key).release();
	    }
	}
}
