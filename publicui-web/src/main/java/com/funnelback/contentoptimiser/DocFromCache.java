package com.funnelback.contentoptimiser;

import java.io.File;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;

public interface DocFromCache {

	String getDocument(ContentOptimiserModel comparison, String cacheUrl, Config config);

	String[] getArgsForSingleDocument(String[] wholeCollectionArgs);

}
