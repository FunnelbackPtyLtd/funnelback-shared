package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.contentoptimiser.fetchers.BldInfoStatsFetcher;
import com.funnelback.contentoptimiser.processors.impl.BldInfoStats;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
@Log
@Component
public class DefaultBldInfoStatsFetcher implements BldInfoStatsFetcher {
	
	@Autowired
	I18n i18n;
	
	public BldInfoStats fetch(ContentOptimiserModel model, Collection collection) throws IOException {
		List<File> bldinfos = new ArrayList<File>();
		
		File indexStem = new File(collection.getConfiguration().getCollectionRoot(), DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX + File.separator + DefaultValues.INDEXFILES_PREFIX);

		Set<String> stemsSeen = new HashSet<String>();

		getBldInfoForStem(indexStem, stemsSeen, bldinfos);
		long totalDocs = 0;
		BigDecimal totalWordsInDocs = new BigDecimal(0);
		
		for(File bldinfo : bldinfos) {
			BldInfoStats stats = readBldInfo(bldinfo);
			totalDocs += stats.getTotalDocuments();
			totalWordsInDocs = totalWordsInDocs.add(new BigDecimal(stats.getTotalDocuments()).multiply(new BigDecimal(stats.getAvgWords())));
		}
		int avgWordsInDoc = 0;
		if(totalDocs != 0) {
		  avgWordsInDoc = totalWordsInDocs.divide(new BigDecimal(totalDocs),RoundingMode.HALF_DOWN).intValue();
		} else {
			log.error("Didn't find any documents reported in the bldinfo files. \".bldinfo\"s were: " + Arrays.toString(bldinfos.toArray(new File[0])) + " index stems examined were " + Arrays.toString(stemsSeen.toArray(new String[0])));
			model.getMessages().add(i18n.tr("error.readingBldinfo"));
		}
		return new BldInfoStats(totalDocs,avgWordsInDoc); 
	}

	private void getBldInfoForStem(File indexStem, Set<String> stemsSeen,
			List<File> bldinfos) throws IOException {
		String indexStemPath = indexStem.getAbsolutePath();
		if(! stemsSeen.contains(indexStemPath)) { 
			stemsSeen.add(indexStemPath);
		
			File bldinfo = new File(indexStem + ".bldinfo");
			if(! bldinfo.exists()) {
				// this is a meta collection, parse the .sdinfo file 
				File sdInfo = new File(indexStemPath +".sdinfo");
				if(! sdInfo.exists()) {
					// neither the bldinfo or sdinfo file exist for this collection
					throw new FileNotFoundException(bldinfo + " or " + sdInfo);
				} else {
					BufferedReader in = new BufferedReader(new FileReader(sdInfo));
					String line;
					while((line = in.readLine()) != null) {
						String[] A = line.split("\\s+");
						getBldInfoForStem(new File(A[0]),stemsSeen,bldinfos);
					}
				}
			} else {
				bldinfos.add(bldinfo);
			}
		}
	}

	private BldInfoStats readBldInfo(File bldinfo) throws IOException {
		long totalDocuments = -1;
		int avgWords = -1;
		
		BufferedReader in = new BufferedReader(new FileReader(bldinfo));
		String line;
		while((line = in.readLine()) != null) {
			if(line.startsWith("Num_docs: ")) {
				totalDocuments = Long.parseLong(line.substring("Num_docs: ".length()));
			}else if(line.startsWith("Average document length: ")) {
				String[] A = line.split("\\s+");
				avgWords = (int)Double.parseDouble(A[3]);
			}
		}
	
		return new BldInfoStats(totalDocuments,avgWords);
	}


}
