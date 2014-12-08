package com.funnelback.publicui.search.service.anchors;

import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;


public interface AnchorsFetcher {

    AnchorModel fetchGeneral(String indexUrl, int docNum, String collectionName, Collection col);

    AnchorModel fetchDetail(String indexUrl, int docNum, String collectionName,String anchortext, int start, Collection col);

}
