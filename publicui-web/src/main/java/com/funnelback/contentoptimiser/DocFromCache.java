package com.funnelback.contentoptimiser;

import java.io.File;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;

public interface DocFromCache {

	String getDocument(UrlComparison comparison, String cacheUrl, Config config);

	String[] getArgsForSingleDocument(String[] wholeCollectionArgs);

}
