package com.funnelback.publicui.test.search.web.controllers.cache;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.utils.DocHdrUtils;
import com.funnelback.publicui.search.web.controllers.CacheController;

public class TRIMStoreCacheTest extends
		AbstractRawBytesCacheControllerTest {

	private static final String COLLECTION_ID = "cache-trim-trimstore";

	@Override
	protected String getPrimaryKey() {
		return "trim://45/1357/1357-0.html";
	}
	
	@Override
	protected void storeContent(RecordAndMetadata<RawBytesRecord> rmd) throws IOException {
		// Noop: The data is already in live/data/45/...
	}
	
	@Override
	protected String getCollectionId() {
		return COLLECTION_ID;
	}
	
	@Override
	protected String getCacheUrl(String primaryKey) {
		return primaryKey;
	}
	
	@Override
	protected RecordAndMetadata<RawBytesRecord> buildRecordAndMetadata()
			throws IOException {
		return new RecordAndMetadata<RawBytesRecord>(
			new RawBytesRecord(
					DocHdrUtils.stripDocHdr(FileUtils.readFileToString(
							new File("src/test/resources/dummy-search_home/data/cache-trim-trimstore/live/data/45/357/1357-0.html.pan.txt"))).getBytes(),
				getPrimaryKey()),
			METADATA);
	}
	
	@Override
	protected void cleanupStore() throws IOException {
		// Noop, data has been setup in live/data/45/...
	}
	
	@Test
	public void testUnknownRecord() throws Exception {
		ModelAndView mav = cacheController.cache(request,
				response,
				configRepository.getCollection(collectionId),
				DefaultValues.PREVIEW_SUFFIX,
				DefaultValues.DEFAULT_FORM,
				"trim://12/345/");
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
		Assert.assertEquals(CacheController.CACHED_COPY_UNAVAILABLE_VIEW, mav.getViewName());
	}

}


