package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import lombok.extern.log4j.Log4j;

@Log4j
public class PropertiesResource extends AbstractSingleFileResource<Properties> {

	public PropertiesResource(File file) {
		super(file);
	}

	@Override
	public Properties parse() throws IOException {
		log.debug("Loading properties from '"+file.getAbsolutePath()+"'");
		Properties props = new Properties();
		FileReader reader = new FileReader(file);
		try {
			props.load(reader);
		} finally {
			reader.close();
		}

		return props;
	}

}
