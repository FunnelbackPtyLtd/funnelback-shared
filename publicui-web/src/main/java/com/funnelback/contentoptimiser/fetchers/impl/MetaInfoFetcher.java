package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

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
	@Setter private MultipleConfigReader<MetaInfo> configReader;
		
	public MetaInfoFetcher(Collection collection,String profileId) {
		rankerOptions = new RankerOptions();
		rankerOptions.consume(collection.getConfiguration().value(Keys.QUERY_PROCESSOR_OPTIONS));

		if(profileId != null) {
			String padreOpts = collection.getProfiles().get(profileId).getPadreOpts();
			if(padreOpts != null) rankerOptions.consume(padreOpts);
		}
		this.collection = collection;
		configReader = new DefaultMultipleConfigReader<MetaInfo>(new MetaInfoConfigReader());
	}

	public MetaInfo get(String metaClass) {
		if(metaInfos.containsKey(metaClass)) return metaInfos.get(metaClass);
		else return new MetaInfo(metaClass,"metadata class '" + metaClass +"'","Try adding more occurrences of the query term to the metadata class",10);  
	}

	public void fetch(File searchHome,String profileName) throws FileNotFoundException {
		if(profileName == null) profileName = DefaultValues.DEFAULT_PROFILE;
		String[] fileNamesToRead = {
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml.default",
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml",		
				collection.getConfiguration().getConfigDirectory() + File.separator + "meta-names.xml",
				collection.getConfiguration().getConfigDirectory() + File.separator + profileName + File.separator + "meta-names.xml",
		}; 
		String[] fileNamesToExpect = {
				fileNamesToRead[0],
		};
		
		metaInfos.putAll(configReader.read(Arrays.asList(fileNamesToRead),new HashSet<String>(Arrays.asList(fileNamesToExpect))));		
	}
}
