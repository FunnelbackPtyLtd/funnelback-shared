package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class DocumentContentModel {
	@Getter @Setter private int totalWords;
	@Getter @Setter private int uniqueWords;
	@Getter @Setter private String commonWords = "[]";
	
	@Getter private final Map<String,Set<String>> termsToStemEquivs = new HashMap<String,Set<String>>();

}
