package com.funnelback.contentoptimiser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultMultipleConfigReader<T> implements MultipleConfigReader<T>{

	private final ConfigReader<T> configReader;

	public DefaultMultipleConfigReader(ConfigReader<T> configReader) {
		this.configReader = configReader;
	}

	@Override
	public Map<String, T> read(List<String> fileNames) {
		Map<String,T> m = new HashMap<String,T>();
		
		for(String file : fileNames) {
			m.putAll(configReader.read(file));
		}
		return m;
	}

	@Override
	public Map<String, T> read(List<String> fileNames, Set<String> mustExist) throws FileNotFoundException {
		for(String fileName : mustExist) {
			File f = new File(fileName);
			if( ! f.exists() ) throw new FileNotFoundException(fileName);
		}		
		return read(fileNames);
	}

}
