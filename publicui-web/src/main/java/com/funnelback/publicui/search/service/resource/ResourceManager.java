package com.funnelback.publicui.search.service.resource;

import java.io.File;
import java.io.IOException;

public interface ResourceManager {

	public <T> T load(File f, ResourceParser<T> p) throws IOException;
	
}
