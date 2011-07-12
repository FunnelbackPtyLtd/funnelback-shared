package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import lombok.Getter;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.contentoptimiser.fetchers.BldInfoStatsFetcher;
import com.funnelback.publicui.search.model.collection.Collection;

public class DefaultBldInfoStatsFetcher implements BldInfoStatsFetcher {

	@Getter private final long totalDocuments;
	@Getter private final int avgWords;
	
	public DefaultBldInfoStatsFetcher(Collection collection) throws IOException {

		File bldinfo = new File(collection.getConfiguration().getCollectionRoot(), DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX + File.separator + DefaultValues.INDEXFILES_PREFIX + ".bldinfo");
		BufferedReader in = new BufferedReader(new FileReader(bldinfo));
		
		long totalDocuments = -1;
		int avgWords = -1;
		
		String line;
		while((line = in.readLine()) != null) {
			if(line.startsWith("Num_docs: ")) {
				totalDocuments = Long.parseLong(line.substring("Num_docs: ".length()));
			}else if(line.startsWith("Average document length: ")) {
				String[] A = line.split("\\s+");
				avgWords = (int)Double.parseDouble(A[3]);
			}
		}
	
		this.totalDocuments = totalDocuments;
		this.avgWords = avgWords;
	}


}
