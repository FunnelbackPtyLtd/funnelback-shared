package com.funnelback.publicui.search.model.anchors;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class AnchorModel {
	@Getter @Setter private String distilledFileName; 
	@Getter @Setter private Integer totalLinks = 0;
	@Getter @Setter private String docNum;
	@Getter @Setter private String collection;
	@Getter @Setter private String error;
	@Getter @Setter private String url;
	
	@Getter @Setter private AnchorDetail detail;
	
	@Getter @Setter private List<AnchorDescription> anchors;
}
