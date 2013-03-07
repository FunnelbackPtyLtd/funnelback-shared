package com.funnelback.contentoptimiser.fetchers;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;

public interface DocFromCache {

    String getDocument(ContentOptimiserModel comparison, String cacheUrl, Config config, String collectionId);

    String[] getArgsForSingleDocument(String[] wholeCollectionArgs);

}
