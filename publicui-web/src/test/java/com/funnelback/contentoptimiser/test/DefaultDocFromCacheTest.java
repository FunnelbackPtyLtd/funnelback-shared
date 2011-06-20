package com.funnelback.contentoptimiser.test;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.exec.OS;
import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.contentoptimiser.DefaultDocFromCache;
import com.funnelback.contentoptimiser.DocFromCache;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;


public class DefaultDocFromCacheTest {

	@Test
	public void testOptionsFromFedGov() {
		String[] originalArgs = {
			    "-F/opt/funnelback/data/fed-gov/live/idx_reindex/index.click.anchors.gz",
			    "-F/opt/funnelback/conf/fed-gov/index.click.anchors.gz",
			    "-ifb",
			    "-MWIPD20000",
			    "-noaltanx",
			    "-nosrcanx",
			    "-W20000",
			    "-big7",
			    "-cleanup",
			    "-hashlog",
			    "-MMF/opt/funnelback/conf/fed-gov/metamap.cfg",
			    "-EM/opt/funnelback/conf/fed-gov/external_metadata.cfg",
			    "-XMF/opt/funnelback/conf/fed-gov/xml.cfg",			
		};
		
		String[] expectedArgs = {
				"-MWIPD20000",
				"-noaltanx",
				"-nosrcanx",
				"-cleanup",
				"-hashlog",
				"-MMF/opt/funnelback/conf/fed-gov/metamap.cfg",
				"-EM/opt/funnelback/conf/fed-gov/external_metadata.cfg",
				"-XMF/opt/funnelback/conf/fed-gov/xml.cfg",
				"-small", 
				"-show_each_word_to_file",
		};
		
		DocFromCache dFromC = new DefaultDocFromCache();
		
		Assert.assertArrayEquals(expectedArgs, dFromC.getArgsForSingleDocument(originalArgs));
		
	}
	
	@Test
	public void testGetDocumentForks() throws FileNotFoundException, EnvironmentVariableException {
		File searchHome = new File("src/test/resources/dummy-search_home");
		DefaultDocFromCache dFromC = new DefaultDocFromCache();
		dFromC.setSearchHome(searchHome);

		
		String ext = ".sh";
		if (OS.isFamilyWindows()) {
			ext = ".bat";
		}		
		String idx = "mock-padre-iw" + ext;
		
		String cacheCgi = "cache" + ((OS.isFamilyWindows()) ? ".bat" : ".cgi");		
		
		SearchQuestion qs = new SearchQuestion();
		qs.setCollection(new Collection("testGetDocumentForks",
				new NoOptionsConfig(searchHome, "testGetDocumentForks")
					.setValue("indexer", idx)
					.setValue(Keys.UI_CACHE_LINK, cacheCgi)
					.setValue(Keys.COLLECTION_ROOT,
							new File(searchHome, "data" + File.separator + "data-repository").toString())));
		
		UrlComparison comparison = new UrlComparison();
		
		
		dFromC.getDocument(comparison, "cache-url", qs.getCollection().getConfiguration());
		Assert.assertTrue("Unexpected messages: " + comparison.getMessages().toString(),comparison.getMessages().isEmpty());
	}
}
