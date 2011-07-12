package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import lombok.Getter;
import lombok.Setter;

public class DocumentContentModel {
	@Getter @Setter private int totalWords;
	@Getter @Setter private int uniqueWords;
	@Getter @Setter private String commonWords;

}
