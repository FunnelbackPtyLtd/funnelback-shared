package com.funnelback.publicui.test.search.service.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.service.index.IndexUpdateLock;

public class IndexUpdateLockTest {
	
	private static final int COLLECTION_COUNT = 3;

	private Config[] config;
	
	
	
	private File getLockFile(Config config) {
		return new File(config.getCollectionRoot()
			 	+ File.separator + DefaultValues.VIEW_LIVE 
            	+ File.separator + DefaultValues.FOLDER_IDX
            	,  Files.Index.UPDATE_LOCK);
	}
	
	private Config getConfig(int i) throws FileNotFoundException{
		String collectionName = "index-update-lock" + i;
		Config config = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), collectionName);
        config.setValue("collection_root",
                new File("src/test/resources/dummy-search_home/data/" + collectionName + "/").getAbsolutePath() );
        config.setValue("collection", collectionName );
        return config;
	}
	
	@Before
	public void before() throws FileNotFoundException {
		config = new Config[COLLECTION_COUNT];
		for (int i = 0; i < COLLECTION_COUNT; i++){
			config[i] = getConfig(i);
			Assert.assertTrue(getLockFile(config[i]).exists());
		}
	}
	
	@Test
	public void oneCollectionOneCall() throws Exception {
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(config[0]), "rw");
		FileLock indexUpdateLock = null;
		try {
			IndexUpdateLock.getIndexUpdateLockInstance().lock(config[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			IndexUpdateLock.getIndexUpdateLockInstance().release(config[0]);
			
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
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(config[0]), "rw");
		FileLock indexUpdateLock = null;
		try {
			IndexUpdateLock.getIndexUpdateLockInstance().lock(config[0]);
			IndexUpdateLock.getIndexUpdateLockInstance().lock(config[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			IndexUpdateLock.getIndexUpdateLockInstance().release(config[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			IndexUpdateLock.getIndexUpdateLockInstance().lock(config[0]);
			IndexUpdateLock.getIndexUpdateLockInstance().release(config[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			IndexUpdateLock.getIndexUpdateLockInstance().release(config[0]);
			
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
	public void IndexUpdateLOckIsASingelton(){
		Assert.assertTrue(IndexUpdateLock.getIndexUpdateLockInstance() == IndexUpdateLock.getIndexUpdateLockInstance());
	}
	
	@Test
	public void ensureDifferentCollectionsHaveDifferentLocks()throws Exception {
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(config[0]), "rw");
		FileLock indexUpdateLock = null;
		try {
			IndexUpdateLock.getIndexUpdateLockInstance().lock(config[0]);
			IndexUpdateLock.getIndexUpdateLockInstance().lock(config[1]);
			
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			IndexUpdateLock.getIndexUpdateLockInstance().release(config[0]);
			
			//Try to lock the file, should pass
			indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);			
			Assert.assertNotNull("Maybe another collection relesed this collection's lock" ,indexUpdateLock);
			
			IndexUpdateLock.getIndexUpdateLockInstance().release(config[1]);
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
		RandomAccessFile indexUpdateLockRandomFile = new RandomAccessFile(getLockFile(config[0]), "rw");
		FileLock indexUpdateLock = null;
		Config otherConfig = getConfig(0);
		Assert.assertFalse(otherConfig == config[0]);
		try {
			IndexUpdateLock.getIndexUpdateLockInstance().lock(config[0]);
			
			//Try to lock the file, should fail
			try {
				indexUpdateLock = indexUpdateLockRandomFile.getChannel().tryLock(0, Long.MAX_VALUE, false);
				Assert.fail("Should not be able to aquire the lock");
			} catch (OverlappingFileLockException e) {}
			Assert.assertNull(indexUpdateLock);
			
			//Release with a differnt Config instance
			IndexUpdateLock.getIndexUpdateLockInstance().release(otherConfig);
			
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
