package com.funnelback.publicui.search.service.anchors;

import java.util.List;

import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;


public interface AnchorsFetcher {

	AnchorModel fetchGeneral(int docNum, Collection collection);

	AnchorModel fetchDetail(int docNum, Collection collection,String anchortext, int start);

}
