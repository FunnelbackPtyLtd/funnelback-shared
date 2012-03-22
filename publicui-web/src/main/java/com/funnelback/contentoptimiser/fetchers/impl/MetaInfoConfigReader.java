package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import com.funnelback.contentoptimiser.ConfigReader;
import com.funnelback.contentoptimiser.processors.impl.MetaInfo;
import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

@Log4j
public class MetaInfoConfigReader implements ConfigReader<MetaInfo> {

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, MetaInfo> read(String file) {
		XStream xstream = new XStream();
		xstream.alias("namesAndSuggestions",Map.class);
		xstream.alias("className", String.class);
		xstream.alias("info", MetaInfo.class);
		xstream.alias("metadataClass", Map.Entry.class);

		File fileInstance = new File(file);
		String xml = null;
		if(fileInstance.exists()) {
			try {	
				xml = Files.toString(fileInstance, Charset.forName("UTF-8"));

			} catch (IOException e) {
				log.error("IOException when reading config file",e);
			}
		}
		if (xml != null) return (Map<String, MetaInfo>) xstream.fromXML(xml);
		else return new HashMap<String,MetaInfo>();
	}

}
