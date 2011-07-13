package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.Getter;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.contentoptimiser.DefaultMultipleConfigReader;
import com.funnelback.contentoptimiser.MultipleConfigReader;
import com.funnelback.contentoptimiser.processors.impl.MetaInfo;
import com.funnelback.contentoptimiser.processors.impl.RankerOptions;
import com.funnelback.publicui.search.model.collection.Collection;

public class MetaInfoFetcher {
	
	@Getter private final RankerOptions rankerOptions;
	private final Map<String,MetaInfo> metaInfos = new HashMap<String,MetaInfo>();
	private final Collection collection;
		
	public MetaInfoFetcher(Collection collection) {
		rankerOptions = new RankerOptions(collection.getConfiguration().value(Keys.QUERY_PROCESSOR_OPTIONS));
		this.collection = collection;
	}

	public MetaInfo get(String metaClass) {
		if(metaInfos.containsKey(metaClass)) return metaInfos.get(metaClass);
		else return new MetaInfo(metaClass,"metadata class '" + metaClass +"'","Try adding more occurrences of the query term to the metadata class",10);  
	}

	public void fetch(File searchHome) throws FileNotFoundException {

		MultipleConfigReader<MetaInfo> configReader = new DefaultMultipleConfigReader<MetaInfo>(new MetaInfoConfigReader());
		
		
		String[] fileNamesToRead = {
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml.default",
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml",		
				collection.getConfiguration().getConfigDirectory() + File.separator + "meta-names.xml",
		}; 
		String[] fileNamesToExpect = {
				fileNamesToRead[0],
		};
		
		metaInfos.putAll(configReader.read(Arrays.asList(fileNamesToRead),new HashSet<String>(Arrays.asList(fileNamesToExpect))));		
	}
}
