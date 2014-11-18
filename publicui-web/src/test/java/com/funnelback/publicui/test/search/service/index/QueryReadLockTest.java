package com.funnelback.publicui.test.search.service.index;

import com.funnelback.common.lock.Lock;
import com.funnelback.common.lock.ThreadSharedFileLock.FileLockException;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.index.DefaultQueryReadLock;
import com.funnelback.publicui.search.service.index.QueryReadLock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public class QueryReadLockTest {
	
	private static final int COLLECTION_COUNT = 3;

	private Collection[] collection;
	
	private QueryReadLock queryReadLock;
	
	private File getLockFile(Collection collection) {
	    return new File(Lock.collectionInstantUpdateLockFile(collection.getConfiguration().getCollectionRoot()));
	}
	
	private Collection getCollection(int i) throws FileNotFoundException{
		String collectionName = "index-update-lock" + i;
		Config config = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), collectionName);
        config.setValue("collection_root",
                new File("src/test/resources/dummy-search_home/data/" + collectionName + "/").getAbsolutePath() );
        config.setValue("collection", collectionName );
        Collection collection = new Collection(collectionName, config);
        return collection;
	}
	
	@Before
	public void before() throws IOException {
		collection = new Collection[COLLECTION_COUNT];
		for (int i = 0; i < COLLECTION_COUNT; i++){
			collection[i] = getCollection(i);
			getLockFile(collection[i]).createNewFile();
			Assert.assertTrue(getLockFile(collection[i]).exists());
		}
		queryReadLock = new DefaultQueryReadLock();
	}
	
	@After
	public void after() throws IOException {
	    for (int i = 0; i < COLLECTION_COUNT; i++){
	        getLockFile(collection[i]).delete();
	    }
	}
	
	@Test
	public void oneCollectionOneCall() throws Exception {
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(collection[0]), "rw");
		FileLock indexUpdateLock = null;
		try {
			queryReadLock.lock(collection[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			queryReadLock.release(collection[0]);
			
			//Try to lock the file, should pass
			indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);			
			Assert.assertNotNull(indexUpdateLock);
		} finally {
			if (indexUpdateLock != null) {
				indexUpdateLock.release();
			}
			if (indexUpdateLockRandomFile != null) {
				indexUpdateLockRandomFile.close();
			}
		}
	}
	
	@Test
	public void oneCollectionThreeCalls() throws Exception {
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(collection[0]), "rw");
		FileLock indexUpdateLock = null;
		try {
			queryReadLock.lock(collection[0]);
			queryReadLock.lock(collection[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			queryReadLock.release(collection[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			queryReadLock.lock(collection[0]);
			queryReadLock.release(collection[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			queryReadLock.release(collection[0]);
			
			//Try to lock the file, should pass
			indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);			
			Assert.assertNotNull(indexUpdateLock);
		} finally {
			if (indexUpdateLock != null) {
				indexUpdateLock.release();
			}
			if (indexUpdateLockRandomFile != null) {
				indexUpdateLockRandomFile.close();
			}
		}
	}
	
	@Test
	public void IndexUpdateLOckIsASingelton() throws FileLockException{
		//We test this by getting two instances if the locking service and locking on the same file.
		//if it is not a sinleton it will through some error
		QueryReadLock otherQueryReadLock = new DefaultQueryReadLock();
		this.queryReadLock.lock(collection[0]);
		//otherQueryReadLock.lock(collection[0]);
		
		//this.queryReadLock.release(collection[0]);
		otherQueryReadLock.release(collection[0]);
	}
	
	@Test
	public void ensureDifferentCollectionsHaveDifferentLocks()throws Exception {
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(collection[0]), "rw");
		FileLock indexUpdateLock = null;
		try {
			queryReadLock.lock(collection[0]);
			queryReadLock.lock(collection[1]);
			
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			queryReadLock.release(collection[0]);
			
			//Try to lock the file, should pass
			indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);			
			Assert.assertNotNull("Maybe another collection relesed this collection's lock" ,indexUpdateLock);
			
			queryReadLock.release(collection[1]);
		} finally {
			if (indexUpdateLock != null) {
				indexUpdateLock.release();
			}
			if (indexUpdateLockRandomFile != null) {
				indexUpdateLockRandomFile.close();
			}
		}
	}
	
	@Test
	/*
	 * We really should not have the case where the instance of the Config for the same collection matters
	 */
	public void checkDifferentConfigObjectsGivesTheSameLock() throws Exception {
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(collection[0]), "rw");
		FileLock indexUpdateLock = null;
		Collection otherCollection = getCollection(0);
		Assert.assertFalse(otherCollection == collection[0]);
		try {
			queryReadLock.lock(collection[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			//Release with a differnt Config instance
			queryReadLock.release(otherCollection);
			
			//Try to lock the file, should pass
			indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);			
			Assert.assertNotNull(indexUpdateLock);
		} finally {
			if (indexUpdateLock != null) {
				indexUpdateLock.release();
			}
			if (indexUpdateLockRandomFile != null) {
				indexUpdateLockRandomFile.close();
			}
		}
	}
	
}
