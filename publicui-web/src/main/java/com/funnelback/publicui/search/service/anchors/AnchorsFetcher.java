package com.funnelback.publicui.search.service.anchors;

import com.funnelback.publicui.search.model.anchors.AnchorModel;


public interface AnchorsFetcher {

	AnchorModel fetchGeneral(int docNum, String collectionName);

	AnchorModel fetchDetail(int docNum, String collectionName,String anchortext, int start);

}
